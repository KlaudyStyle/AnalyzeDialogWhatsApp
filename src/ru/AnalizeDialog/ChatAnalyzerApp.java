package ru.AnalizeDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class ChatAnalyzerApp {

    private static List<Message> allMessages;
    private static Map<LocalDate, Map<String, Integer>> aggregatedData;
    private static ChartPanel chartPanel;
    private static JLabel avgResponseLabel;
    private static JLabel maxResponseLabel;
    private static JLabel minResponseLabel;

    public static List<String> information = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Использование: java ChatAnalyzerApp <путь_к_файлу_чата.txt>");
            return;
        }

        String filePath = args[0];
        allMessages = ChatParser.parseChat(filePath);
        if (allMessages.isEmpty()) {
            System.out.println("Чат пуст или не удалось его распарсить.");
            return;
        }

        aggregatedData = MessageAggregator.aggregateMessagesByDay(allMessages);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Анализатор переписки");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.getContentPane().setBackground(Color.LIGHT_GRAY);
            frame.setLayout(new BorderLayout());

            JPanel filterPanel = new JPanel();
            filterPanel.setBackground(Color.LIGHT_GRAY);
            filterPanel.setLayout(new FlowLayout());

            JLabel startDateLabel = new JLabel("Начальная дата (дд.MM.гггг):");
            JTextField startDateField = new JTextField(10);

            JLabel endDateLabel = new JLabel("Конечная дата (дд.MM.гггг):");
            JTextField endDateField = new JTextField(10);

            JButton applyFilterButton = new JButton("Применить фильтр");

            // Новая кнопка для анализа другого чата
            JButton analyzeChatButton = new JButton("Анализировать переписку");

            filterPanel.add(startDateLabel);
            filterPanel.add(startDateField);
            filterPanel.add(endDateLabel);
            filterPanel.add(endDateField);
            filterPanel.add(applyFilterButton);
            filterPanel.add(analyzeChatButton); // Добавляем кнопку на панель

            frame.add(filterPanel, BorderLayout.NORTH);

            // Инициализация панели графика
            chartPanel = new ChartPanel(aggregatedData);
            JScrollPane scrollPane = new JScrollPane(chartPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            scrollPane.getViewport().setBackground(Color.LIGHT_GRAY);

            frame.add(scrollPane, BorderLayout.CENTER);

            // Панель метрик
            JPanel metricsPanel = new JPanel();
            metricsPanel.setBackground(Color.LIGHT_GRAY);
            metricsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            avgResponseLabel = new JLabel();
            maxResponseLabel = new JLabel();
            minResponseLabel = new JLabel();

            metricsPanel.add(avgResponseLabel);
            metricsPanel.add(Box.createHorizontalStrut(20)); // Отступ между метками
            metricsPanel.add(maxResponseLabel);
            metricsPanel.add(Box.createHorizontalStrut(20)); // Отступ между метками
            metricsPanel.add(minResponseLabel);

            frame.add(metricsPanel, BorderLayout.SOUTH);

            // Вычисление и отображение метрик
            updateResponseMetrics(aggregatedData, allMessages, aggregatedData);

            // Обработчик кнопки фильтрации
            applyFilterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String startDateStr = startDateField.getText().trim();
                    String endDateStr = endDateField.getText().trim();

                    LocalDate startDate = null;
                    LocalDate endDate = null;

                    try {
                        if (!startDateStr.isEmpty()) {
                            startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                        }
                        if (!endDateStr.isEmpty()) {
                            endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                        }
                    } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(frame, "Пожалуйста, введите даты в формате дд.MM.гггг", "Неверный формат даты", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Фильтрация сообщений по дате
                    List<Message> filteredMessages = MessageFilter.filterMessagesByDate(allMessages, startDate, endDate);
                    aggregatedData = MessageAggregator.aggregateMessagesByDay(filteredMessages);

                    // Обновление графика
                    chartPanel.updateData(aggregatedData);
                    chartPanel.revalidate();
                    chartPanel.repaint();

                    // Вычисление и отображение метрик для отфильтрованных данных
                    updateResponseMetrics(aggregatedData, filteredMessages, aggregatedData);
                }
            });

            // Обработчик кнопки "Анализировать переписку"
            analyzeChatButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openAndAnalyzeChat(frame);
                }
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    private static void openAndAnalyzeChat(JFrame parentFrame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл чата для анализа");
        int userSelection = fileChooser.showOpenDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToOpen = fileChooser.getSelectedFile();
            String filePath = fileToOpen.getAbsolutePath();
            List<Message> newMessages = ChatParser.parseChat(filePath);
            if (newMessages.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Чат пуст или не удалось его распарсить.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            allMessages = newMessages;
            aggregatedData = MessageAggregator.aggregateMessagesByDay(allMessages);

            chartPanel.updateData(aggregatedData);
            chartPanel.revalidate();
            chartPanel.repaint();

            updateResponseMetrics(aggregatedData, allMessages, aggregatedData);
        }
    }

    private static void updateResponseMetrics(Map<LocalDate, Map<String, Integer>> aggregatedData, List<Message> messages, Map<LocalDate, Map<String, Integer>> aggregatedData2) {
    	information.clear();
        HashMap<String, Integer> countMessages = new HashMap<>();
        HashMap<String, Integer> countHearts = new HashMap<>();
        HashMap<String, Integer> countEmojis = new HashMap<>();
        HashMap<String, Integer> countLinks = new HashMap<>();
        HashMap<String, Integer> countMedias = new HashMap<>();
        HashMap<String, Integer> countWords = new HashMap<>();
        HashMap<String, Integer> countEdittedMessage = new HashMap<>();
        
        for(Message m : messages) {
        	String message = m.getContent();
        	int heartsMessage = countHearts(message);
        	int icountEmojis = countEmojis(message);
        	int icountLinks = countLinks(message);
        	int words = countWords(message);
        	boolean countMedia = isMediaMessage(message);
        	countHearts.put(m.getSender(), countHearts.getOrDefault(m.getSender(), 0) + heartsMessage);
        	countLinks.put(m.getSender(), countLinks.getOrDefault(m.getSender(), 0) + icountLinks);
        	countEmojis.put(m.getSender(), countEmojis.getOrDefault(m.getSender(), 0) + icountEmojis);
        	countWords.put(m.getSender(), countWords.getOrDefault(m.getSender(), 0) + words);
        	
        	countMessages.put(m.getSender(), countMessages.getOrDefault(m.getSender(), 0) + 1);
        	if(countMedia) countMedias.put(m.getSender(), countMedias.getOrDefault(m.getSender(), 0) + 1);
        	if(message.contains("<Сообщение изменено>")) countEdittedMessage.put(m.getSender(), countEdittedMessage.getOrDefault(m.getSender(), 0) + 1);
        }

        Duration totalResponseTime = Duration.ZERO;
        int responseCount = 0;

        for (int i = 1; i < messages.size(); i++) {
            Message previsMessage = messages.get(i - 1);
            Message currentMessage = messages.get(i);

            if (!currentMessage.getSender().equals(previsMessage.getSender())) {
                Duration responseTime = Duration.between(previsMessage.getTimestamp(), currentMessage.getTimestamp());
                totalResponseTime = totalResponseTime.plus(responseTime);
                responseCount++;
            }
        }

        if (responseCount > 0) {
            Duration averageResponseTime = totalResponseTime.dividedBy(responseCount);
            information.add("Средняя скорость ответа: " + averageResponseTime.toMinutes() + " минут");
        } else {
            System.out.println("Ответов нет или недостаточно данных для расчёта.");
        }
    	if(aggregatedData2 != null) {
    		information.add("Количество дней (с активностью): " + aggregatedData2.size());
    	}
        information.add("Количество сообщений: " + countMessages.toString());
        information.add("Количество сердечек: " + countHearts.toString());
        information.add("Количество смайликов: " + countEmojis.toString());
        information.add("Количество медиа файлов: " + countMedias.toString());
        information.add("Количество ссылок: " + countLinks.toString());
        information.add("Количество измененных сообщений: " + countEdittedMessage.toString());
    }
    
    public static int countWords(String str) {
        str = str.trim();
        if (str.isEmpty()) {
            return 0;
        }
        String[] words = str.split("\\s+");
        return words.length;
    }
    
	private static boolean isMediaMessage(String line) {
		String lowerCaseLine = line.toLowerCase();
		return lowerCaseLine.contains("<без медиафайлов>");
	}
	
	private static int countLinks(String line) {
		String urlPattern = "((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*)";
		Pattern pattern = Pattern.compile(urlPattern);
		Matcher matcher = pattern.matcher(line);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}
    
    private static int countEmojis(String text) {
        String emojiPattern = "[\uD83C-\uDBFF\uDC00-\uDFFF]";
        Pattern pattern = Pattern.compile(emojiPattern);
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
	private static int countHearts(String line) {
		if (line == null || line.trim().isEmpty()) {
			return 0;
		}
		int heartCount = 0;
		String[] hearts = { "❤️", "🤍", "😘", "😍", "🥰"};

		for (String heart : hearts) {
			int index = line.indexOf(heart);
			while (index != -1) {
				heartCount++;
				line = line.substring(index + heart.length());
				index = line.indexOf(heart);
			}
		}

		return heartCount;
	}
}
