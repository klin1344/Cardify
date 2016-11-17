package com.klin1344.cardify;



public class Card {
	private String cardWord;
	private String cardDefinition;
	
	public Card(String word, String definition) {
		cardWord = word;
		cardDefinition = definition;
	}
	
	public void setWord(String word) {
		cardWord = word;
	}
	
	public void setDefinition(String definition) {
		cardDefinition = definition;
	}
	
	public String getWord() {
		return cardWord;
	}
	
	public String getDefinition() {
		return cardDefinition;
	}

	/*public void swapCards(int i, int j) {
		Collections.swap(cardWord, i, j);
		Collections.swap(cardDefinition, i, j);
	}
	
	public void shuffleCards() {
		Random random = new Random();
		long seed = random.nextLong();
		random.setSeed(seed);
		Collections.shuffle(cardWord, random);
		random.setSeed(seed);
		Collections.shuffle(cardDefinition, random);
	}
	
	public void sortAlphabetically() {
		TreeMap treeMap = new TreeMap();
		for (int i = 0; i < cardWord.size(); i++) {
			treeMap.put(cardWord, cardDefinition);
		}
		int pos = 0;
		for (String> key : treeMap.keySet()) {
		    cardDefinition.get(i) = key;
		}
	}*/
}
