package datapath.procurementdata.documentparser.service.parser;

import datapath.procurementdata.documentparser.dao.entity.Document;
import datapath.procurementdata.documentparser.dao.entity.ParsingResult;
import datapath.procurementdata.documentparser.dao.entity.ParsingResult.PartResult;
import datapath.procurementdata.documentparser.dao.entity.ParsingResult.PositionResult;
import datapath.procurementdata.documentparser.dao.entity.ParsingResult.RegexResult;
import datapath.procurementdata.documentparser.dao.service.DocumentDaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
public class ContentParser {

    private final DocumentDaoService daoService;

    private final Map<String, Pattern> PATTERNS;
    private final List<String> TRIGGERS;
    private final boolean enabled;

    private final Pattern CLEAN_PATTERN = Pattern.compile("(?i)\\p{L}+([’\\-]\\p{L}+)?");

    public ContentParser(@Value("${document.parsing.enable}") boolean enabled,
                         DocumentDaoService daoService) throws IOException {
        this.enabled = enabled;
        this.daoService = daoService;
        this.PATTERNS = Files.readAllLines(Path.of("regexes.txt"))
                .stream()
                .collect(toMap(identity(), this::preparePattern));
        this.TRIGGERS = Files.readAllLines(Path.of("triggers.txt"));
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

        String[] parts = toParts(d.getText());

        List<PartResult> partResults = parse(parts);

        if (isEmpty(partResults)) return null;

        ParsingResult result = new ParsingResult();
        result.setPartResults(partResults);

        return result;
    }

    private List<PartResult> parse(String[] parts) {
        List<PartResult> result = new ArrayList<>();
        for (String part : parts) {
            String cleanedPart = clean(part);
            if (StringUtils.isBlank(cleanedPart))
                continue;

            List<RegexResult> regexResults = new ArrayList<>();
            for (Map.Entry<String, Pattern> pattern : PATTERNS.entrySet()) {
                List<PositionResult> positionResults = new ArrayList<>();

                Matcher matcher = pattern.getValue().matcher(cleanedPart.toLowerCase());
                while (matcher.find()) {
                    PositionResult positionResult = new PositionResult();
                    int start = matcher.start();
                    positionResult.setStartIndex(start);
                    positionResult.setSelectedText(StringUtils.substring(cleanedPart, start - 50, start + matcher.group().length() + 50));
                    positionResults.add(positionResult);
                }

                if (!isEmpty(positionResults)) {
                    RegexResult regexResult = new RegexResult();
                    regexResult.setRegex(pattern.getKey());
                    regexResult.setPositionResult(positionResults);
                    regexResults.add(regexResult);
                }
            }

            if (isEmpty(regexResults)) continue;

            PartResult partResult = new PartResult();
            partResult.setSymbolCount(cleanedPart.length());
            partResult.setRegexResult(regexResults);

            int minPosition = regexResults.stream()
                    .flatMap(r -> r.getPositionResult().stream())
                    .min(Comparator.comparingInt(PositionResult::getStartIndex))
                    .map(PositionResult::getStartIndex)
                    .orElseThrow(RuntimeException::new);

            int max = regexResults.stream()
                    .flatMap(r -> r.getPositionResult().stream())
                    .max(Comparator.comparingInt(PositionResult::getStartIndex))
                    .map(PositionResult::getStartIndex)
                    .orElseThrow(RuntimeException::new);

            int min = minPosition - 400;
            max = max + 200;

            if (min < 0) min = 0;
            if (max > cleanedPart.length()) max = cleanedPart.length();

            String selectedText = StringUtils.substring(cleanedPart, 0, max);

//            partResult.setSelectedText(substringByTriggers(selectedText, minPosition));
            partResult.setSelectedText(selectedText);

            result.add(partResult);
        }
        return result;
    }

    private String substringByTriggers(String selectedText, int minPosition) {
        if (StringUtils.isBlank(selectedText)) return null;

        Integer startIndex = null;
        for (String trigger : TRIGGERS) {
            if (containsIgnoreCase(selectedText, trigger)) {
                int i = StringUtils.indexOfIgnoreCase(selectedText, trigger);
                if ((isNull(startIndex) || i < startIndex) && minPosition > i)
                    startIndex = i;
            }
        }

        return StringUtils.substring(selectedText, isNull(startIndex) ? 0 : startIndex);
    }

    private String clean(String part) {
        Matcher matcher = CLEAN_PATTERN.matcher(part);

        List<String> res = new LinkedList<>();

        while (matcher.find()) {
            res.add(matcher.group());
        }

        return String.join(" ", res);
    }

    private String[] toParts(String text) {
        String start = StringUtils.substring(text, 0, 150);

        if (containsIgnoreCase(start, "додаток"))
            return new String[]{text};

        return text.split("Додаток|додаток|ДОДАТОК");
    }

    private Pattern preparePattern(String trigger) {
        trigger = trigger.trim().toLowerCase();

        String[] triggerParts = trigger.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String triggerPart : triggerParts) {
            if ("*".equals(triggerPart)) {
                builder.append(".{0,50}\\s*");
                continue;
            }

            if (triggerPart.contains("*")) {
                triggerPart = triggerPart.replaceAll("\\*$", "\\\\p{L}{0,10}");
                triggerPart = triggerPart.replaceAll("\\*", "[^ ]{1,10}");
            }

            builder.append(triggerPart).append(" ");
        }

        return Pattern.compile(builder.toString().trim());
    }
}
