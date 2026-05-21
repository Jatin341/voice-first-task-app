package com.senpiper.voicetask.util;

import com.senpiper.voicetask.model.Task;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VoiceParser {

    public Task parse(String voiceText) {
        Task task = new Task();
        String cleaned = voiceText.toLowerCase().trim();

        task.setTitle(extractTitle(cleaned));
        task.setDescription(voiceText);
        task.setDueDate(extractDueDate(cleaned));

        return task;
    }

    private String extractTitle(String text) {
        String[] fillers = {
                "remind me to", "remind me", "create a task to",
                "add a task to", "add task", "i need to",
                "don't forget to", "dont forget to",
                "set a reminder to", "make a note to",
                "schedule a", "schedule"
        };
        for (String filler : fillers) {
            text = text.replace(filler, "").trim();
        }

        // Remove due date portion
        text = text.replaceAll("by (next |this )?(monday|tuesday|wednesday|thursday|friday|saturday|sunday)", "").trim();
        text = text.replaceAll("by (today|tomorrow|next week|next month)", "").trim();
        text = text.replaceAll("by \\d{1,2}(st|nd|rd|th)?( of)? \\w+", "").trim();
        text = text.replaceAll("(today|tomorrow|by tonight)", "").trim();

        if (text.isEmpty()) text = "New Task";

        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private LocalDateTime extractDueDate(String text) {
        LocalDate today = LocalDate.now();

        if (text.contains("today") || text.contains("tonight"))
            return today.atTime(23, 59);
        if (text.contains("tomorrow"))
            return today.plusDays(1).atTime(23, 59);
        if (text.contains("next week"))
            return today.plusWeeks(1).atTime(23, 59);
        if (text.contains("next month"))
            return today.plusMonths(1).atTime(23, 59);
        if (text.contains("next monday"))
            return getNextDay(today, DayOfWeek.MONDAY).atTime(23, 59);
        if (text.contains("next tuesday"))
            return getNextDay(today, DayOfWeek.TUESDAY).atTime(23, 59);
        if (text.contains("next wednesday"))
            return getNextDay(today, DayOfWeek.WEDNESDAY).atTime(23, 59);
        if (text.contains("next thursday"))
            return getNextDay(today, DayOfWeek.THURSDAY).atTime(23, 59);
        if (text.contains("next friday"))
            return getNextDay(today, DayOfWeek.FRIDAY).atTime(23, 59);
        if (text.contains("next saturday"))
            return getNextDay(today, DayOfWeek.SATURDAY).atTime(23, 59);
        if (text.contains("next sunday"))
            return getNextDay(today, DayOfWeek.SUNDAY).atTime(23, 59);

        // Pattern: "by 25th" or "by june 5"
        Pattern p = Pattern.compile("by (\\d{1,2})(st|nd|rd|th)?");
        Matcher m = p.matcher(text);
        if (m.find()) {
            int day = Integer.parseInt(m.group(1));
            LocalDate date = today.withDayOfMonth(day);
            if (date.isBefore(today)) date = date.plusMonths(1);
            return date.atTime(23, 59);
        }

        // Default: 7 days
        return today.plusDays(7).atTime(23, 59);
    }

    private LocalDate getNextDay(LocalDate from, DayOfWeek day) {
        return from.with(java.time.temporal.TemporalAdjusters.next(day));
    }
}