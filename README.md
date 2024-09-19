# Working Calculator

## Description

This is a simple Android calculator application written in Java. It supports basic mathematical operations such as addition, subtraction, multiplication, division, and percentage calculations. The app also features text copying and pasting, as well as gesture support for deleting the last digit.

## Key Features

- **Addition, subtraction, multiplication, and division.**
- **Percentage calculations.**
- **Delete the last digit using a swipe gesture.**
- **Copying and pasting text.**
- **Change the sign of a number to positive or negative.**

## How to Use the App

1. **Input Numbers**:
   - Tap the number buttons to input digits.
   - Use the dot button to enter decimal numbers.

2. **Mathematical Operations**:
   - Choose one of the operations: addition (`+`), subtraction (`-`), multiplication (`×`), division (`÷`).
   - After entering a number and selecting an operation, input a second number and press `=` to see the result.

3. **Percentage**:
   - Press the `%` button to calculate the percentage of a number.

4. **Clear**:
   - Use the `AC` button to reset all values and start over.

5. **Delete the Last Digit**:
   - Swipe across the screen from left to right to delete the last digit.

6. **Copy and Paste**:
   - Tap on the text field, then press and hold to open the context menu from which you can copy or paste text.

## Features

- If you attempt to divide by zero, the app will display an error.
- If a number is too long, the font size will decrease to fit the number on the screen.
- Vibration feedback is used to confirm button presses.

## Requirements

- **Android Studio** to build the project.
- **Minimum SDK version**: 21 (Android 5.0).

## Installation

1. Clone the repository with the source code.
2. Open the project in Android Studio.
3. Sync the dependencies by clicking "Sync Project with Gradle Files."
4. Launch the emulator or connect an Android device and click "Run."

## Frequently Asked Questions

**What should I do if I accidentally entered the wrong number?**  
Swipe across the screen from left to right to delete the last digit.

**Why am I seeing "Error"?**  
You likely attempted an invalid operation, such as dividing by zero. Simply press `AC` to start over.

