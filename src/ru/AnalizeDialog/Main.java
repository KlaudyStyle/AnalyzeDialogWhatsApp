package ru.AnalizeDialog;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Main {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

	public static void main(String[] args) {
		String filePath = "C:\\Users\\Klaudy\\Desktop\\ms.txt";
		Map<String, Integer> userMessageCount = new HashMap<>();
		Map<String, Integer> userWordCount = new HashMap<>();
		Map<String, Long> userLastMessageTime = new HashMap<>();
		Map<String, Long> userTotalResponseTime = new HashMap<>();
		Map<String, Integer> userResponseCount = new HashMap<>();
		Map<String, Integer> userHeartCount = new HashMap<>();
		Map<String, Integer> userMediaCount = new HashMap<>();
		Map<String, Integer> userEditedCount = new HashMap<>();
		Map<String, Integer> userLinkCount = new HashMap<>();
		Map<String, Integer> userSmilesCount = new HashMap<>();
		TreeMap<Long, Integer> timePeriods = new TreeMap<>();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineSplit = line.split(" - ", 2);
				if (lineSplit.length == 2) {
					String timestamp = lineSplit[0].trim();
					String userLine = lineSplit[1].trim();
					String user = getUserFromLine(userLine);
					if (user != null && isTimestampValid(timestamp)) {
						long messageTime = dateFormat.parse(timestamp).getTime();
						if (userLastMessageTime.containsKey(user)) {
							long lastTime = userLastMessageTime.get(user);
							long responseTime = messageTime - lastTime;
							userTotalResponseTime.put(user,
									userTotalResponseTime.getOrDefault(user, 0L) + responseTime);
							userResponseCount.put(user, userResponseCount.getOrDefault(user, 0) + 1);
						}
						userLastMessageTime.put(user, messageTime);
						userMessageCount.put(user, userMessageCount.getOrDefault(user, 0) + 1);
						int wordCount = countWords(userLine);
						userWordCount.put(user, userWordCount.getOrDefault(user, 0) + wordCount);
						int heartCount = countHearts(userLine);
						userHeartCount.put(user, userHeartCount.getOrDefault(user, 0) + heartCount);
						
						int smilesCount = countEmojis(removeFirstWord(userLine));
						userSmilesCount.put(user, userSmilesCount.getOrDefault(user, 0) + smilesCount);

						if (isMediaMessage(userLine)) {
							userMediaCount.put(user, userMediaCount.getOrDefault(user, 0) + 1);
						}
						if (userLine.contains("<Сообщение изменено>")) {
							userEditedCount.put(user, userEditedCount.getOrDefault(user, 0) + 1);
						}

						int linkCount = countLinks(userLine);
						userLinkCount.put(user, userLinkCount.getOrDefault(user, 0) + linkCount);

						long hourPeriod = messageTime / (60 * 60 * 1000);
						timePeriods.put(hourPeriod, timePeriods.getOrDefault(hourPeriod, 0) + 1);
					}
				}
			}
			System.out.println("Самые активные периоды:");
			timePeriods.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).limit(10)
					.forEach(entry -> {
						System.out.println("Период: " + new Date(entry.getKey() * 60 * 60 * 1000) + " Сообщения: "
								+ entry.getValue());
					});

			System.out.println("\nСреднее время ответа пользователей:");
			userResponseCount.forEach((user, count) -> {
				long totalResponseTime = userTotalResponseTime.getOrDefault(user, 0L);
				if (count > 0) {
					System.out.println(user + ": " + (totalResponseTime / count) / 1000 + " секунд");
				}
			});

			System.out.println("\nКоличество сообщений пользователей:");
			userMessageCount.forEach((user, count) -> {
				System.out.println(user + ": " + count + " сообщений");
			});

			System.out.println("\nКоличество слов пользователей:");
			userWordCount.forEach((user, count) -> {
				System.out.println(user + ": " + count + " слов");
			});

			System.out.println("\nКоличество смайликов с сердечками пользователей:");
			userHeartCount.forEach((user, count) -> System.out.println(user + ": " + count + " сердечек"));
			
			System.out.println("\nКоличество смайликов пользователей:");
			userSmilesCount.forEach((user, count) -> System.out.println(user + ": " + count + " смайликов"));

			System.out.println("\nКоличество медиафайлов пользователей:");
			userMediaCount.forEach((user, count) -> System.out.println(user + ": " + count + " медиафайлов"));

			System.out.println("\nКоличество измененных сообщений:");
			userEditedCount.forEach((user, count) -> System.out.println(user + ": " + count + " измененных сообщений"));

			System.out.println("\nКоличество отправленных ссылок:");
			userLinkCount.forEach((user, count) -> System.out.println(user + ": " + count + " ссылок"));

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
    public static String removeFirstWord(String input) {
        // Используем регулярное выражение для поиска первого слова
        return input.replaceFirst("^\\S+\\s*", "");
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
        // Регулярное выражение для поиска смайликов
        String emojiPattern = "[\uD83C-\uDBFF\uDC00-\uDFFF]";
        Pattern pattern = Pattern.compile(emojiPattern);
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

	private static boolean isMediaMessage(String line) {
		String lowerCaseLine = line.toLowerCase();
		return lowerCaseLine.contains("<без медиафайлов>");
	}

	private static String getUserFromLine(String line) {
		int userEndIndex = line.indexOf(":");
		if (userEndIndex > 0) {
			String userWithEmojis = line.substring(0, userEndIndex).trim();
			return userWithEmojis.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();
		}
		return null;
	}

	private static int countWords(String line) {
		if (line == null || line.trim().isEmpty()) {
			return 0;
		}
		int messageStartIndex = line.indexOf(":");
		if (messageStartIndex >= 0) {
			String messageContent = line.substring(messageStartIndex + 1).trim();
			if (messageContent.startsWith("<")) {
				return 0;
			}
			String[] words = messageContent.split("\\s+");
			return words.length;
		}
		return 0;
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

	private static boolean isTimestampValid(String timestamp) {
		try {
			dateFormat.parse(timestamp);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
}