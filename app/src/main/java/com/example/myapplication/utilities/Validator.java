package com.example.myapplication.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    public static boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) {
        // (?=.*[0-9]) ==> a digit must occur at least once
        // (?=.*[a-z]) ==> a lower case character must occur at least once
        // (?=.*[0-9]) ==> a upper case character must occur at least once
        // .{5,10}     ==> password length [min:5, max:10]
        String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{5,10}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
