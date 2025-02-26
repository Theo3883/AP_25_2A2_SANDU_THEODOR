public class Main {
    public static void main(String[] args) {

        //ex 1
        System.out.println("Hello, World!");

        //ex 2

        String[] languages = {"C", "C++", "C#", "Python", "Go", "Rust", "JavaScript", "PHP", "Swift", "Java"};

        //ex 3
        int number = (int)(Math.random() * 1000000);
        System.out.println("generated number is "+number);

        //ex 4

        int new_number = calc_actions(number);

        //ex 5
        int result= (int) 2e9;
        while(result > 9)
        {
             result = calc_digit_sum(new_number);
             new_number = result;
        }

        System.out.println("result is " + result);

        //ex 6
        System.out.println("Willy-nilly, this semester I will learn " + languages[result]);

    }

    private static int calc_actions(int newNumber) {
        newNumber = newNumber * 3;
        newNumber = newNumber + Integer.parseInt("10101",2);
        newNumber = newNumber + Integer.parseInt("FF",16);
        newNumber = newNumber * 6;
        return newNumber;
    }

    private static int calc_digit_sum(int number)
    {
        int sum = 0;
        while(number > 0)
        {
            sum = sum + number % 10;
            number = number / 10;
        }
        return sum;
    }

}