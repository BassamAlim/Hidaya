package bassamalim.hidaya.models;

public class QuizResultQuestion {

    private final int questionNumber;
    private final String questionText;
    private final int correctAnswer;
    private final int chosenAnswer;
    private final String answer1;
    private final String answer2;
    private final String answer3;

    public QuizResultQuestion(int questionNumber, String questionText, int correctAnswer,
                              int chosenAnswer, String answer1, String answer2, String answer3) {
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.chosenAnswer = chosenAnswer;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public int getChosenAnswer() {
        return chosenAnswer;
    }

    public String getAnswer1() {
        return answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public String getAnswer3() {
        return answer3;
    }
}
