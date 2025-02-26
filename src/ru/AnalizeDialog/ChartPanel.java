package ru.AnalizeDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class ChartPanel extends JPanel {
    private Map<LocalDate, Map<String, Integer>> data;
    private List<String> senders;
    private int padding = 60;
    private int labelPadding = 50;
    private Color axisColor = Color.BLACK;
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final int numberYDivisions = 10;

    public ChartPanel(Map<LocalDate, Map<String, Integer>> data) {
        this.data = data;
        this.senders = getAllSenders(data);
        this.setBackground(Color.LIGHT_GRAY);
    }

    private List<String> getAllSenders(Map<LocalDate, Map<String, Integer>> data) {
        Set<String> senderSet = new TreeSet<>();
        for (Map<String, Integer> dailyData : data.values()) {
            senderSet.addAll(dailyData.keySet());
        }
        return new ArrayList<>(senderSet);
    }

    public void updateData(Map<LocalDate, Map<String, Integer>> newData) {
        this.data = newData;
        this.senders = getAllSenders(newData);
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.setColor(Color.BLACK);
            g.drawString("Нет данных для отображения в выбранном промежутке времени.", getWidth() / 2 - 150, getHeight() / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int maxMessages = 0;
        for (Map<String, Integer> dailyData : data.values()) {
            for (Integer count : dailyData.values()) {
                maxMessages = Math.max(maxMessages, count);
            }
        }

        int graphHeight = height - 2 * padding - labelPadding - 50;

        for (int i = 0; i <= numberYDivisions; i++) {
            int y = graphHeight - (i * (graphHeight) / numberYDivisions) + padding;
            g2.setColor(gridColor);
            g2.drawLine(padding + labelPadding, y, width - padding, y);
            g2.setColor(axisColor);
            String yLabel = ((int) ((maxMessages * ((i * 1.0) / numberYDivisions)))) + "";
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            g2.drawString(yLabel, padding + labelPadding - labelWidth - 5, y + (metrics.getHeight() / 2) - 3);
        }

        List<LocalDate> dates = new ArrayList<>(data.keySet());
        Collections.sort(dates);

        Map<YearMonth, LocalDate> firstDatesOfMonths = new LinkedHashMap<>();
        for (LocalDate date : dates) {
            YearMonth ym = YearMonth.from(date);
            firstDatesOfMonths.putIfAbsent(ym, date);
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("ru"));

        for (Map.Entry<YearMonth, LocalDate> entry : firstDatesOfMonths.entrySet()) {
            LocalDate firstDate = entry.getValue();
            int index = dates.indexOf(firstDate);

            if (index == -1) continue; 
            double groupWidth = (width - 2 * padding - labelPadding) / (double) dates.size();
            int x = padding + labelPadding + (int) (index * groupWidth + groupWidth / 2);

            g2.setColor(gridColor);
            g2.drawLine(x, padding, x, graphHeight + padding);

            String monthLabel = firstDate.format(monthFormatter);
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(monthLabel);

            AffineTransform original = g2.getTransform();
            g2.rotate(-Math.PI / 4, x, graphHeight + padding + 15);
            g2.setColor(axisColor);
            g2.drawString(monthLabel, x - labelWidth / 2, graphHeight + padding + 20);
            g2.setTransform(original);
        }

        g2.drawLine(padding + labelPadding, padding, padding + labelPadding, graphHeight + padding);
        g2.drawLine(padding + labelPadding, graphHeight + padding, width - padding, graphHeight + padding);

        Map<String, Color> senderColors = generateColors(senders);

        int groupWidth = (width - 2 * padding - labelPadding) / dates.size();
        int barWidth = senders.size() > 0 ? groupWidth / senders.size() - 2 : 10;

        int xPos = padding + labelPadding;
        for (LocalDate date : dates) {
            Map<String, Integer> dailyData = data.get(date);

            int senderIndex = 0;
            for (String sender : senders) {
                int count = dailyData.getOrDefault(sender, 0);
                int barHeight = maxMessages > 0 ? (int) ((count * 1.0 / maxMessages) * graphHeight) : 0;
                int y = graphHeight + padding - barHeight;
                g2.setColor(senderColors.get(sender));
                int barX = xPos + senderIndex * (barWidth + 2);
                g2.fillRect(barX, y, barWidth, barHeight);
                senderIndex++;
            }
            xPos += groupWidth;
        }

        int legendX = width - padding - 150;
        int legendY = padding;
        for (String sender : senders) {
            g2.setColor(senderColors.get(sender));
            g2.fillRect(legendX, legendY, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString(sender, legendX + 15, legendY + 10);
            legendY += 15;
        }

        g2.setColor(Color.BLACK);
        int infoY = graphHeight + padding + 40;

        int i = infoY;
        int x = 0;
        int c = 0;
        for(String str : ChatAnalyzerApp.information) {
        	g2.drawString(str, padding + labelPadding + x, i);
        	if(c >= 4) {
        		i = infoY;
        		c = 0;
        		x += 500;
        	}
        	c++;
        	i += 15;
        }
        Font boldFont = new Font("Serif", Font.BOLD, 17);
        g.setFont(boldFont);
        g.drawString("WhatsApp chat analyzer by Klaudy (v1.0)", 30, 30);
    }

    private Map<String, Color> generateColors(List<String> senders) {
        Map<String, Color> colorMap = new HashMap<>();
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW, Color.GRAY, Color.DARK_GRAY};
        for (int i = 0; i < senders.size(); i++) {
            colorMap.put(senders.get(i), colors[i % colors.length]);
        }
        return colorMap;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 600);
    }
}
