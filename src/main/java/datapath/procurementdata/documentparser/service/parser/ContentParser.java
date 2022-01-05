package datapath.procurementdata.documentparser.service.parser;

import datapath.procurementdata.documentparser.dao.entity.Document;
import datapath.procurementdata.documentparser.dao.entity.ParsingResult;
import datapath.procurementdata.documentparser.dao.service.DocumentDaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
public class ContentParser {

    private final DocumentDaoService daoService;

    private final Map<String, Pattern> PATTERNS;
    private final boolean enabled;


    public ContentParser(@Value("${document.parsing.enable}") boolean enabled,
                         DocumentDaoService daoService) throws IOException {
        this.enabled = enabled;
        this.daoService = daoService;
        this.PATTERNS = Files.readAllLines(Path.of("regexes.txt"))
                .stream()
                .collect(toMap(identity(), this::preparePattern));
    }

    @Scheduled(fixedDelay = 1000 * 60)
    private void parse() {
        if (!enabled) return;

        log.info("Content parsing started");

        List<Document> documents;

        while (!isEmpty(documents = daoService.getNotParsed())) {
            documents.forEach(d -> {
                log.info("Parsing {}", d.getId());
                try {
                    ParsingResult result = parse(d);
                    if (nonNull(result)) {
                        result.setId(d.getId());
                        daoService.save(result);
                    }
                } catch (Exception e) {
                    log.error("Error", e);
                }

                d.setParsed(true);

                try {
                    daoService.save(d);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        log.info("Content parsing finished");
    }

    private ParsingResult parse(Document d) {
        if (StringUtils.isBlank(d.getText())) return null;

        List<ParsingResult.RegexResult> result = new ArrayList<>();
        for (Map.Entry<String, Pattern> pattern : PATTERNS.entrySet()) {
            List<ParsingResult.PositionResult> positionResults = new ArrayList<>();

            Matcher matcher = pattern.getValue().matcher(d.getText().toLowerCase());
            while (matcher.find()) {
                ParsingResult.PositionResult positionResult = new ParsingResult.PositionResult();
                int start = matcher.start();
                positionResult.setStartIndex(start);
                positionResult.setSelectedText(StringUtils.substring(d.getText(), start - 50, start + matcher.group().length() + 50));
                positionResults.add(positionResult);
            }

            if (!isEmpty(positionResults)) {
                ParsingResult.RegexResult regexResult = new ParsingResult.RegexResult();
                regexResult.setRegex(pattern.getKey());
                regexResult.setPositionResult(positionResults);
                result.add(regexResult);
            }
        }

        if (isEmpty(result)) return null;

        ParsingResult parsingResult = new ParsingResult();
        parsingResult.setSymbolCount(d.getText().length());
        parsingResult.setRegexResult(result);

        int min = result.stream()
                .flatMap(r -> r.getPositionResult().stream())
                .min(Comparator.comparingInt(ParsingResult.PositionResult::getStartIndex))
                .map(ParsingResult.PositionResult::getStartIndex)
                .orElseThrow(RuntimeException::new);

        int max = result.stream()
                .flatMap(r -> r.getPositionResult().stream())
                .max(Comparator.comparingInt(ParsingResult.PositionResult::getStartIndex))
                .map(ParsingResult.PositionResult::getStartIndex)
                .orElseThrow(RuntimeException::new);

        min = min - 200;
        max = max + 200;

        if (min < 0) min = 0;
        if (max > d.getText().length()) max = d.getText().length();

        parsingResult.setSelectedText(StringUtils.substring(d.getText(), min, max));

        return parsingResult;
    }

    private Pattern preparePattern(String trigger) {
        trigger = trigger.trim().toLowerCase();
        trigger = trigger.replaceAll(" \\* ", "(\\\\s+.{0,50}?\\\\s+|\\\\s)");
        trigger = trigger.replaceAll("\\*", "\\\\p{L}*");
        return Pattern.compile(trigger);
    }
}
