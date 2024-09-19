package com.example.workingcalculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;


import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private GestureDetector gestureDetector;
    private Vibrator vibrator;

    private static final int BASE_TEXT_SIZE = 96;
    private static final int SIZE_FOR_LENGTH_7 = 79;
    private static final int SIZE_FOR_LENGTH_8 = 70;
    private static final int SIZE_FOR_LENGTH_9 = 63;

    private String oldNumber = "";
    private String operation = "";
    private boolean isNew = true;

    private BigDecimal currentResult = BigDecimal.ZERO;
    private boolean hasPendingOperation = false;

    private final Map<Integer, String> buttonMap = new HashMap<>();
    private final Map<Integer, String> buttonCommandMap = new HashMap<>();
    private final Map<Integer, Runnable> menuActionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        editText = findViewById(R.id.editText);
        if (editText == null) {
            Log.e("MainActivity", "EditText not found.");
            return;
        }
        editText.setShowSoftInputOnFocus(false);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() > e2.getX()) {
                    removeLastDigit();
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        editText.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });

        initializeButtonMaps();
        setupButtonListeners();
        registerForContextMenu(editText);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.editText) {
            menu.setHeaderTitle("Edit Options");
            menu.add(Menu.NONE, 1, 1, "Copy");
            menu.add(Menu.NONE, 2, 2, "Paste");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Copy
                copyText();
                return true;
            case 2: // Paste
                pasteText();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void copyText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", editText.getText().toString());
        clipboard.setPrimaryClip(clip);
    }

    private void pasteText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            editText.setText(item.getText());
        }
    }

    private void initializeButtonMaps() {
        buttonMap.put(R.id.one, "1");
        buttonMap.put(R.id.two, "2");
        buttonMap.put(R.id.three, "3");
        buttonMap.put(R.id.four, "4");
        buttonMap.put(R.id.five, "5");
        buttonMap.put(R.id.six, "6");
        buttonMap.put(R.id.seven, "7");
        buttonMap.put(R.id.eight, "8");
        buttonMap.put(R.id.nine, "9");
        buttonMap.put(R.id.zero, "0");
        buttonMap.put(R.id.plus_or_minus, "-");
        buttonMap.put(R.id.dot, ".");

        buttonCommandMap.put(R.id.plus, "+");
        buttonCommandMap.put(R.id.minus, "-");
        buttonCommandMap.put(R.id.multiply, "×");
        buttonCommandMap.put(R.id.divide, "÷");
        buttonCommandMap.put(R.id.ac, "AC");
        buttonCommandMap.put(R.id.equal, "=");
        buttonCommandMap.put(R.id.percent, "%");
    }

    private void setupButtonListeners() {
        for (Integer buttonId : buttonMap.keySet()) {
            findViewById(buttonId).setOnClickListener(view -> {
                vibrate();
                clickNumber(view);
            });
        }

        for (Integer commandId : buttonCommandMap.keySet()) {
            findViewById(commandId).setOnClickListener(view -> {
                vibrate();
                clickOperation(view);
            });
        }

        findViewById(R.id.ac).setOnClickListener(view -> {
            vibrate();
            clearDesk(view);
        });

        findViewById(R.id.percent).setOnClickListener(view -> {
            vibrate();
            clickPercent(view);
        });

        findViewById(R.id.equal).setOnClickListener(view -> {
            vibrate();
            clickResult(view);
        });

        findViewById(R.id.plus_or_minus).setOnClickListener(view -> {
            vibrate();
            clickPlusMinus(view);
        });

        findViewById(R.id.dot).setOnClickListener(view -> {
            vibrate();
            clickDot(view);
        });
    }

    private void removeLastDigit() {
        String text = editText.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (text.length() > 1) {
                // Если в строке больше одного символа, удаляем последний
                text = text.substring(0, text.length() - 1);
            } else {
                // Если в строке один символ, очищаем ее
                text = "0";
            }
            updateText(text);
        }
    }


    public void clickNumber(View view) {
        if (isNew) {
            editText.setText("");
            isNew = false;
        }

        String number = editText.getText().toString().replace(" ", "").replace(".", "");
        if (number.length() >= 9) {
            return;
        }

        Integer viewId = view.getId();
        String buttonText = buttonMap.get(viewId);

        if (buttonText != null) {
            String newNumber = editText.getText().toString().equals("0") ? buttonText : editText.getText().toString() + buttonText;
            updateText(newNumber);
        }
    }

    public void clickOperation(View view) {
        String newNumberString = editText.getText().toString().trim().replaceAll("[^\\d.-]", "");

        try {
            BigDecimal newNumberDecimal = new BigDecimal(newNumberString);

            if (hasPendingOperation) {
                if (!isNew) {
                    // Если не был введён новый номер, выполняем предыдущую операцию
                    performOperation(newNumberDecimal);
                }
            } else {
                // Иначе просто сохраняем текущее число как результат
                currentResult = newNumberDecimal;
            }

            // Обновляем операцию и устанавливаем флаг
            Integer viewId = view.getId();
            operation = buttonCommandMap.get(viewId);
            hasPendingOperation = true;

            // Устанавливаем флаг isNew на true для следующего ввода
            isNew = true;

        } catch (NumberFormatException e) {
            editText.setText("Error");
        }
    }


    private void performOperation(BigDecimal newNumber) {
        if (operation == null || operation.isEmpty()) {
            updateText("Error");
            return;
        }

        switch (operation) {
            case "+":
                currentResult = currentResult.add(newNumber);
                break;
            case "-":
                currentResult = currentResult.subtract(newNumber);
                break;
            case "×":
                currentResult = currentResult.multiply(newNumber);
                break;
            case "÷":
                if (newNumber.compareTo(BigDecimal.ZERO) == 0) {
                    updateText("Error");
                    hasPendingOperation = false;  // Сброс флага операции
                    currentResult = null;  // Очищаем текущий результат, чтобы показать ошибку
                    return;
                } else {
                    currentResult = currentResult.divide(newNumber, 9, RoundingMode.HALF_UP);
                }
                break;
            default:
                updateText("Error");
                hasPendingOperation = false;  // Сброс флага операции
                return;
        }
        updateText(currentResult.stripTrailingZeros().toPlainString());
    }


    public void clickResult(View view) {
        String newNumberString = editText.getText().toString().trim().replaceAll("[^\\d.-]", "");
        if (currentResult == null) {
            updateText("Error");
            return;
        }
        try {
            BigDecimal newNumberDecimal = new BigDecimal(newNumberString);
            if (hasPendingOperation) {
                // Выполняем последнюю операцию
                performOperation(newNumberDecimal);
                hasPendingOperation = false;  // Сбрасываем флаг операции
            } else {
                currentResult = newNumberDecimal;
            }

            // Сбрасываем текущую операцию и старое число
            operation = "";
            oldNumber = "";

            editText.setTextColor(Color.BLACK);

            // Возвращаем исходный цвет через 1 секунду (1000 мс)
            new Handler().postDelayed(() -> {
                editText.setTextColor(Color.WHITE);
            }, 10);

            // Обновляем отображаемый результат
            updateText(currentResult.stripTrailingZeros().toPlainString());

            // Устанавливаем флаг на true для нового ввода
            isNew = true;

        } catch (NumberFormatException e) {
            currentResult = BigDecimal.ZERO;  // Сброс результата
            hasPendingOperation = false;
            updateText("Error");// Сброс флага операции
        } catch (Exception e) {
            currentResult = BigDecimal.ZERO;  // Сброс результата
            hasPendingOperation = false;
            updateText("Error");// Сброс флага операции
        }
    }


    @SuppressLint("SetTextI18n")
    public void clickPercent(View view) {
        // Отримати текст з EditText, видалити непотрібні символи
        String oldNumber = editText.getText().toString().trim().replaceAll("[^\\d.-]", "");

        // Перевірити, чи є текст порожнім або "0"
        if (TextUtils.isEmpty(oldNumber) || oldNumber.equals("0")) {
            editText.setTextColor(Color.BLACK);

            // Возвращаем исходный цвет через 1 секунду (1000 мс)
            new Handler().postDelayed(() -> {
                editText.setTextColor(Color.WHITE);
            }, 10);

            updateText("0"); // Встановити "0", якщо нічого не введено
            return;
        }

        boolean hasMinus = oldNumber.startsWith("-"); // Перевірити, чи є знак мінус

        try {
            // Конвертація рядка в BigDecimal
            BigDecimal number = new BigDecimal(oldNumber);

            // Обчислення відсотка
            BigDecimal result = number.divide(BigDecimal.valueOf(100), 9, RoundingMode.HALF_UP);

            // Форматування результату у вигляді рядка
            String resultText = result.stripTrailingZeros().toPlainString();

            // Додавання знака мінус, якщо потрібно
            if (hasMinus && !resultText.startsWith("-")) {
                resultText = "-" + resultText;
            }


            // Оновлення тексту в EditText
            updateText(resultText);
        } catch (NumberFormatException e) {
            updateText("Error");
        }
    }

    public void clearDesk(View view) {
        currentResult = BigDecimal.ZERO; // Инициализируем currentResult значением 0
        editText.setText("0"); // Устанавливаем текст "0" в поле
        editText.setTextSize(BASE_TEXT_SIZE); // Устанавливаем размер шрифта
        isNew = true; // Устанавливаем флаг для нового ввода

        // Сброс флага операции и старого числа
        hasPendingOperation = false;
        operation = "";
        oldNumber = "";

        // Изменяем цвет текста на красный
        editText.setTextColor(Color.BLACK);

        // Возвращаем исходный цвет через 1 секунду (1000 мс)
        new Handler().postDelayed(() -> {
            editText.setTextColor(Color.WHITE);
        }, 10);
    }


    public void clickPlusMinus(View view) {
        // Отримання тексту з editText
        String number = editText.getText().toString().trim();

        // Перевірка, чи текст є порожнім
        if (TextUtils.isEmpty(number) || number.equals("0")) {
            return; // Нічого не робити, якщо текст порожній або є "0"
        }

        // Перевірка, чи текст вже має знак "-"
        if (number.startsWith("-")) {
            // Якщо має, то видаляємо знак "-"
            number = number.substring(1);
        } else {
            // Інакше додаємо знак "-"
            number = "-" + number;
        }

        // Оновлення тексту в editText
        updateText(number);
    }

    public void clickDot(View view) {
        String number = editText.getText().toString();
        if (!number.contains(".")) {
            number += ".";
        }
        updateText(number);
    }

    private void adjustTextSize(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        int textSize;

        if (text.equals("Error")) {
            textSize = BASE_TEXT_SIZE; // Устанавливаем размер шрифта для "Error"
        } else {
            String cleanedText = text.replaceAll("[\\s.]+", "");
            int length = cleanedText.length();

            if (length >= 1 && length <= 6) {
                textSize = BASE_TEXT_SIZE;
            } else if (length >= 7 && length <= 9) {
                switch (length) {
                    case 7:
                        textSize = SIZE_FOR_LENGTH_7;
                        break;
                    case 8:
                        textSize = SIZE_FOR_LENGTH_8;
                        break;
                    case 9:
                        textSize = SIZE_FOR_LENGTH_9;
                        break;
                    default:
                        textSize = SIZE_FOR_LENGTH_9;
                        break;
                }
            } else {
                textSize = SIZE_FOR_LENGTH_9;
            }
        }

        editText.setTextSize(textSize);
    }


    private String formatTextWithSpaces(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder formattedText = new StringBuilder();
        int dotIndex = text.indexOf('.');

        if (dotIndex != -1) {
            String beforeDot = text.substring(0, dotIndex);
            String afterDot = text.substring(dotIndex + 1);
            formattedText.append(formatNumberBeforeDot(beforeDot));
            formattedText.append('.').append(formatNumberAfterDot(afterDot));
        } else {
            formattedText.append(formatNumberBeforeDot(text));
        }

        return formattedText.toString();
    }


    private String formatNumberBeforeDot(String number) {
        boolean hasMinus = number.startsWith("-"); // Проверяем наличие знака минуса
        number = number.replaceAll("[^\\d]", ""); // Оставляем только цифры

        if (number.isEmpty()) {
            return number; // Возвращаем пустую строку, если входная строка пустая
        }

        StringBuilder formatted = new StringBuilder();
        int length = number.length();

        // Добавляем пробелы после каждых 3 символов
        for (int i = 0; i < length; i++) {
            if (i > 0 && (length - i) % 3 == 0) {
                formatted.append(' ');
            }
            formatted.append(number.charAt(i));
        }

        // Добавляем знак минуса обратно, если он был
        if (hasMinus) {
            return "-" + formatted.toString();
        }

        return formatted.toString();
    }


    private String formatNumberAfterDot(String number) {
        return number.replaceAll("[^\\dE]", "");
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100);
        }
    }

    public void updateText(String text) {
        if ("Error".equals(text)) {
            editText.setText(text);
        } else {
            String formattedText = formatTextWithSpaces(text);
            adjustTextSize(text);
            editText.setText(formattedText);  // Выводим отформатированный текст с пробелами
        }
    }
}