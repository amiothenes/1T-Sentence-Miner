# T1-Sentence-Generator
Finds the lowest hanging fruit in your immersion automatically and adds it straight to your Anki deck.

A tool made for automatic sentence mining to save time for immersion that would be otherwise used up by making flashcards. It uses a text file, where the immersion text would be inserted, and reads it to find 1T sentence cards and uses web scraping to find definitions from online dictionaries. It uses AnkiConnect to automatically put the card into your deck when done. This tool is in the path with Refold, Mia, Migaku.  Become a polyglot!

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/lAAWfZo-kBQ/0.jpg)](https://www.youtube.com/watch?v=lAAWfZo-kBQ)

## Installation
### Video
Coming soon.

### Instructions
1. [Install Java](https://java.com/en/download/) if you don't have it already
2. Clone this repository and unzip the folder
3. Set up the path for your .jar file
    * Open .bat file on Windows and change [PATH] to the folder's path on your PC. Example: `C:\Users\name\folder\T1SentencesFinder.jar`
    * Open .command file on Mac and change [PATH] to the folder's path on your Mac. Example `/Users/name/folder/Testing/T1SentencesFinder.jar`
4. Run the .bat or .command file
5. If it's running without errors, it's time to fill the learnWords.txt with words you know
    1. Export your filtered-only-known-words Anki deck as a .txt file
    2. Set it in Excel or Google Sheets
    3. Make sure to find the row where only the target language word is present ([make it without caps](https://decapitalize.eu/) or articles)
    4. Copy the list of words into learnedWords.txt with one word per line. Like this: ![alt text](https://i.imgur.com/haAgGKN.png "One word per line")
6. Set the config.txt, don't change the "true" and "false" lines but change the last 3rd and 2nd line. Third last line: name it to your Anki Deck and a `::` if you want to add a subdeck. Second last line: name it to your note type.
    1. While you can download a sample note type designed for the app, you can use your own but make sure it has the following fields in order: 1. Sentence, 2. Target Word, 3. Definition, 4. Source.
    2. The last line of the config file is the dictionary that you want to use. The first character is either `m` or `b` for monolingual and bilingual and the last two characters are the language code: `de` for German or `es` for Spanish. ![alt text](https://i.imgur.com/5E5tj1g.png "config.txt")
7. Install the [AnkiConnect](https://ankiweb.net/shared/info/2055492159) Anki addon. This addon is required to make automatic cards that will be added straight into your chosen deck after the 1T sentences were found.

## Guide and Features
### Steps to use the 1T Sentence Generator
1. Grab text out of your immersion and copy into corpus.txt
2. Run the .bat or .command file
3. Open Anki and make sure AnkiConnect is activated.
4. The app will ask you for a source of your text, whatever you type here will show up on the card to give you context where you gotten the generated 1T Sentence from.
5. After adding, press [enter] and it should start loading.
6. After loading, it should say that it has added words that belong in a 1T sentence and is not a known word of yours based on learnedWords.txt.
    * This will automatically add the word into a sentence card in your deck as well as update learnedWords.txt.
7. It should also give a list of words that does not have a definition from your chosen dictionary but still has a 1T sentence. I recommend to choose "save to file" all the time.
8. Now, some unknown words for you with good context (1T sentence) have been added to Anki, go review them!
![alt text](https://i.imgur.com/DotVghO.png "1T Sentence in Anki")

### Extra Features
#### Source Dialogue
* When you are in the source dialogue, you can:
    1. Add `*` at the beginning of your source to skip the loading of text. This will disable the addition of known words in the text not mentioned in learnedWords.txt.
    2. Add `%` at the beginning of your source to enable "Prelecture Plan".
    The initial characters will be removed and not added to the source.
#### Prelecture Plan
Say you want to read a book but your comprehension is below 75%, which is low and not recommended to read through. You can boost your comprehension of the book before reading by studying words that show up frequently in its pages. This feature gives you a list of of 1T, 2T, 3T, and 4T sentences that you will study in Anki before reading so that you are aware of the words. This would make the understanding of the text easier than if you didn't study the text's unknown words prior.

![alt text](https://i.imgur.com/hJQIlde.png "Prelecture Plan")
Here, the UI will be in English and in place of `?` there should be unicode arrows for you.

## Supported Dictionaries and Languages
* Monolingual German (DWDS.NET)

*Please request your language and whether you want the monolingual or bilingual dictionary support, so that it can be added.*

## Resources
Here you can download a frequency list for various languages to fill your learnedWords.txt and the Note Type that I designed for this app.
[Click here to get various frequency lists.](https://github.com/melling/LanguageLearning)

<a id="raw-url" href="https://raw.githubusercontent.com/Amiothenes/1T-Sentence-Miner/master/Deck.apkg">Download Sample Anki Notetype (.apkg)</a>

## Errors/Issues
Please report them in the issues tab.
If you see `?` in random places make sure you have UTF-8 encoding on all the .txt files as well as in Command Prompt or Terminal.

## Future
* Set up many dictionaries for many languages
