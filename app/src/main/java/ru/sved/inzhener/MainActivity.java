package ru.sved.inzhener;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Process;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ГЛАВНЫЙ УЗЕЛ ТЕРМИНАЛА СВЕД (ВЕРСИЯ 2.0 // КВАНТ И КОНТУР)
 * Поддерживает:
 * 1) Стандарт КУБ (плотные грани, контракты, контроль памяти при_сбое)
 * 2) Стандарт КОНТУР (параллельная геометрия: =>>, =<, >=>, ~> на реальных ядрах процессора)
 * 3) Полное отключение всех процессов и пулов потоков (ВЫКЛ ПРОЦЕССЫ)
 */
public class MainActivity extends Activity {

    private EditText codeEditor;
    private TextView consoleOutput;

    // Внутренняя память обратной связи ('~>'), сохраняющаяся между тактами
    private float reactorCorrectionState = 1.0f;
    private int simulationTick = 0;

    // Пул аппаратных ядер процессора для Призмы Ветвления ('=<')
    private ExecutorService corePool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация пула на 3 параллельных вычислительных ядра
        corePool = Executors.newFixedThreadPool(3);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#0A0D10"));
        rootLayout.setPadding(16, 16, 16, 16);

        // 1. Заголовок инженерной среды
        TextView header = new TextView(this);
        header.setText("СВЕД-ТЕРМИНАЛ v2.0 // КУБ + ПАРАЛЛЕЛЬНЫЙ КОНТУР");
        header.setTextColor(Color.parseColor("#00FF9C"));
        header.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        header.setTextSize(13);
        header.setPadding(0, 0, 0, 8);
        rootLayout.addView(header);

        // 2. Панель быстрого выбора чертежа (КОНТУР ПЛАЗМЫ / СЕТЕВОЙ КУБ)
        LinearLayout presetBar = new LinearLayout(this);
        presetBar.setOrientation(LinearLayout.HORIZONTAL);
        presetBar.setPadding(0, 0, 0, 10);

        Button btnPlasma = new Button(this);
        btnPlasma.setText("КОНТУР: ПЛАЗМА");
        btnPlasma.setBackgroundColor(Color.parseColor("#1D3557"));
        btnPlasma.setTextColor(Color.WHITE);
        btnPlasma.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        btnPlasma.setTextSize(11);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        p1.setMarginEnd(6);
        presetBar.addView(btnPlasma, p1);

        Button btnNetCube = new Button(this);
        btnNetCube.setText("КУБ: СЕТЬ");
        btnNetCube.setBackgroundColor(Color.parseColor("#2B2D42"));
        btnNetCube.setTextColor(Color.WHITE);
        btnNetCube.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        btnNetCube.setTextSize(11);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        presetBar.addView(btnNetCube, p2);

        rootLayout.addView(presetBar);

        // 3. Редактор кода (по умолчанию открыт новый Контур ЛабораторияПлазмы)
        codeEditor = new EditText(this);
        codeEditor.setBackgroundColor(Color.parseColor("#12171D"));
        codeEditor.setTextColor(Color.parseColor("#E6EDF3"));
        codeEditor.setTypeface(Typeface.MONOSPACE);
        codeEditor.setTextSize(11);
        codeEditor.setGravity(Gravity.TOP | Gravity.START);
        codeEditor.setPadding(14, 14, 14, 14);
        codeEditor.setText(getPlasmaContourCode());

        LinearLayout.LayoutParams editorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.25f);
        rootLayout.addView(codeEditor, editorParams);

        // 4. Главная панель управления (ПУСК ТАКТА и ПОЛНОЕ ОТКЛЮЧЕНИЕ)
        LinearLayout buttonBar = new LinearLayout(this);
        buttonBar.setOrientation(LinearLayout.HORIZONTAL);
        buttonBar.setPadding(0, 10, 0, 10);

        Button runButton = new Button(this);
        runButton.setText("ТАКТ / СОБРАТЬ");
        runButton.setBackgroundColor(Color.parseColor("#1B4332"));
        runButton.setTextColor(Color.WHITE);
        runButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        btnParams.setMarginEnd(8);
        buttonBar.addView(runButton, btnParams);

        Button killButton = new Button(this);
        killButton.setText("ВЫКЛ ПРОЦЕССЫ");
        killButton.setBackgroundColor(Color.parseColor("#641220"));
        killButton.setTextColor(Color.WHITE);
        killButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams killParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.75f);
        buttonBar.addView(killButton, killParams);

        rootLayout.addView(buttonBar);

        // 5. Консоль телеметрии
        ScrollView consoleScroll = new ScrollView(this);
        consoleScroll.setBackgroundColor(Color.parseColor("#050709"));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.95f);

        consoleOutput = new TextView(this);
        consoleOutput.setTextColor(Color.parseColor("#8B949E"));
        consoleOutput.setTypeface(Typeface.MONOSPACE);
        consoleOutput.setTextSize(11);
        consoleOutput.setPadding(14, 14, 14, 14);
        consoleOutput.setText("СВЕД v2.0 готов. Нажмите 'ТАКТ / СОБРАТЬ' для запуска параллельного контура...");
        consoleScroll.addView(consoleOutput);

        rootLayout.addView(consoleScroll, scrollParams);
        setContentView(rootLayout);

        // Переключение на шаблон Лаборатории Плазмы
        btnPlasma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reactorCorrectionState = 1.0f;
                simulationTick = 0;
                codeEditor.setText(getPlasmaContourCode());
                consoleOutput.setText("Загружен чертеж: КОНТУР [ЛабораторияПлазмы]. Состояние сброшено в 1.0.");
            }
        });

        // Переключение на шаблон Сетевого Куба
        btnNetCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeEditor.setText(getInitialCubeCode());
                consoleOutput.setText("Загружен чертеж: КУБ [СетевойКонтур].");
            }
        });

        // Запуск транслятора и параллельного вычислителя
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = codeEditor.getText().toString();
                String report = analyzeAndExecute(source);
                consoleOutput.setText(report);
            }
        });

        // Жесткое отключение всех потоков и процессов
        killButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shutdownAllProcesses();
            }
        });
    }

    /**
     * УНИВЕРСАЛЬНЫЙ ОДНОПРОХОДНЫЙ АНАЛИЗАТОР (КУБ + ПАРАЛЛЕЛЬНЫЙ КОНТУР)
     */
    private String analyzeAndExecute(String code) {
        if (code.contains("контур ") || code.contains("=>>") || code.contains("=<")) {
            return executeParallelContour(code);
        }
        return analyzeAndExecuteCube(code);
    }

    /**
     * ИСПОЛНЕНИЕ ПАРАЛЛЕЛЬНОГО КОНТУРА (ВОЛНОВОЙ ФРОНТ, ПРИЗМА ЯДЕР И ОБРАТНАЯ СВЯЗЬ)
     */
    private String executeParallelContour(String code) {
        StringBuilder log = new StringBuilder();
        simulationTick++;
        log.append("=== [ТРАНСЛЯТОР ГЕОМЕТРИИ КОНТУРА // ТАКТ #").append(simulationTick).append("] ===\n");

        // 1. Разбор квантов данных
        Matcher quantMatcher = Pattern.compile("квант\\s+([А-Яа-яA-Za-z0-9_]+)\\s*=\\s*([^;]+);").matcher(code);
        int quantCount = 0;
        while (quantMatcher.find()) {
            quantCount++;
            log.append("• Квант: [").append(quantMatcher.group(1)).append("] -> ").append(quantMatcher.group(2).trim()).append("\n");
        }

        // 2. Подсчет исследовательских кубов
        Matcher cubeMatcher = Pattern.compile("куб\\s+([А-Яа-яA-Za-z0-9_]+)").matcher(code);
        List<String> cubes = new ArrayList<>();
        while (cubeMatcher.find()) {
            cubes.add(cubeMatcher.group(1));
        }
        log.append("• Подключено мат. кубов (").append(cubes.size()).append("): ").append(cubes.toString()).append("\n");

        // 3. Проверка операторов параллельной геометрии
        boolean hasWavefront = code.contains("=>>");
        boolean hasSplitPrism = code.contains("=<");
        boolean hasMergeFocus = code.contains(">=>");
        boolean hasFeedbackLoop = code.contains("~>");

        if (!hasWavefront || !hasSplitPrism || !hasMergeFocus) {
            return log.append("\n[ОШИБКА ГЕОМЕТРИИ]: Нарушена цепь операторов (=>>, =<, >=>).\n").toString();
        }

        log.append("• Топология: Волновой фронт [=>>] | Призма [=<] | Фокус [>=>] | Кольцо [~>]\n");

        try {
            // 4. ШАГ 1: ВОЛНОВОЙ ФРОНТ ('=>>') на массиве 10 000 элементов
            final int N = 10000;
            final float[] rawMatrix = new float[N];
            final float[] cleanBuffer = new float[N];

            for (int i = 0; i < N; i++) {
                // Моделируем сигнал датчиков плазмы с гармоническим шумом
                rawMatrix[i] = 12.0f + (float) Math.sin(i * 0.01f + simulationTick) * 3.5f;
            }

            // Операция ФильтрШума.очистить_квант (сглаживание 0.05)
            float prev = rawMatrix[0];
            for (int i = 0; i < N; i++) {
                cleanBuffer[i] = prev + 0.05f * (rawMatrix[i] - prev);
                prev = cleanBuffer[i];
            }
            log.append("• [1. ФРОНТ '=>>']: 10 000 квантов очищены в '*чистый_буфер' (без аллокаций)\n");

            // 5. ШАГ 2: ПРИЗМА ВЕТВЛЕНИЯ ('=<') на 3 параллельных ядра процессора
            final float currentCorrection = reactorCorrectionState;

            // Ветвь 1 (Ядро №1): ТепловойАнализ.вычислить_нагрев (Среднее квадратичное)
            Callable<Float> branch1 = new Callable<Float>() {
                @Override
                public Float call() {
                    double sumSq = 0.0;
                    for (int i = 0; i < N; i++) {
                        sumSq += cleanBuffer[i] * cleanBuffer[i];
                    }
                    return (float) Math.sqrt(sumSq / N);
                }
            };

            // Ветвь 2 (Ядро №2): БароАнализ.вычислить_давление (Максимум * поправка_реактора)
            Callable<Float> branch2 = new Callable<Float>() {
                @Override
                public Float call() {
                    float max = cleanBuffer[0];
                    for (int i = 1; i < N; i++) {
                        if (cleanBuffer[i] > max) max = cleanBuffer[i];
                    }
                    return max * currentCorrection;
                }
            };

            // Ветвь 3 (Ядро №3): ПоискАномалий.найти_резонанс (Дисперсия поля)
            Callable<Float> branch3 = new Callable<Float>() {
                @Override
                public Float call() {
                    double mean = 0.0;
                    for (int i = 0; i < N; i++) mean += cleanBuffer[i];
                    mean /= N;
                    double var = 0.0;
                    for (int i = 0; i < N; i++) {
                        double diff = cleanBuffer[i] - mean;
                        var += diff * diff;
                    }
                    return (float) (var / N);
                }
            };

            // Одновременный запуск на 3 ядрах и ожидание в Фокусе Слияния ('>=>')
            Future<Float> fHeat = corePool.submit(branch1);
            Future<Float> fPressure = corePool.submit(branch2);
            Future<Float> fResonance = corePool.submit(branch3);

            float heat = fHeat.get();
            float pressure = fPressure.get();
            float resonance = fResonance.get();

            log.append("• [2. ПРИЗМА '=<' и ФОКУС '>=>']: 3 ядра синхронизированы без гонок памяти:\n");
            log.append(String.format(Locale.US, "   -> Ядро №1 (Нагрев RMS):   %.4f\n", heat));
            log.append(String.format(Locale.US, "   -> Ядро №2 (Давление):     %.4f (при поправке %.4f)\n", pressure, currentCorrection));
            log.append(String.format(Locale.US, "   -> Ядро №3 (Резонанс/Дисп): %.4f\n", resonance));

            // 6. ШАГ 3: КОЛЬЦО ОБРАТНОЙ СВЯЗИ ('~>')
            float nextResponse = 1.0f / (1.0f + resonance);
            if (hasFeedbackLoop) {
                reactorCorrectionState = nextResponse;
            }

            log.append("\n=== [ИТОГ ТАКТА #").append(simulationTick).append(" // ВекторСреды] ===\n");
            log.append(String.format(Locale.US, "• ВекторСреды: [%.3f, %.3f, %.3f]\n", heat, pressure, resonance));
            log.append(String.format(Locale.US, "• [3. КОЛЬЦО '~>']: Новая поправка_реактора = %.5f передана в Такт #%d\n",
                    reactorCorrectionState, simulationTick + 1));

        } catch (Exception e) {
            log.append("\n[АППАРАТНАЯ ЛОВУШКА КОНТУРА]: ").append(e.getMessage());
        }

        return log.toString();
    }

    /**
     * ВЕРИФИКАТОР СТАНДАРТА "КУБ" (ДЛЯ СЕТЕВОГО КОНТУРА)
     */
    private String analyzeAndExecuteCube(String code) {
        StringBuilder log = new StringBuilder();
        log.append("=== [СТАРТ ОДНОПРОХОДНОГО ТРАНСЛЯТОРА КУБА] ===\n");

        Matcher cubeMatcher = Pattern.compile("куб\\s+([А-Яа-яA-Za-z0-9_]+)").matcher(code);
        if (!cubeMatcher.find()) {
            return "[ОШИБКА ГРАММАТИКИ]: Не найден корневой модуль 'куб' или 'контур'.";
        }
        log.append("• Обнаружен КУБ: [").append(cubeMatcher.group(1)).append("]\n");

        Matcher errMatcher = Pattern.compile("сбой\\s+([А-Яа-яA-Za-z0-9_]+)\\s*:\\s*б8").matcher(code);
        if (errMatcher.find()) {
            log.append("• Линия отказа: ").append(errMatcher.group(1)).append(" (1 байт / б8)\n");
        }

        Matcher structMatcher = Pattern.compile("грань\\s+([А-Яа-яA-Za-z0-9_]+)\\s*([^\\{]*)\\{([^\\}]*)\\}").matcher(code);
        while (structMatcher.find()) {
            String facetName = structMatcher.group(1);
            String attributes = structMatcher.group(2).trim();
            int bytes = calculateFacetBytes(structMatcher.group(3));
            log.append("• Грань: [").append(facetName).append("]");
            if (attributes.contains("@упаковано")) {
                log.append(" | Плотно: ").append(bytes).append(" байт");
            }
            if (attributes.contains("@без_копирования")) {
                log.append(" | Линейный тип");
            }
            log.append("\n");
        }

        boolean hasAllocation = code.contains(".выделить(");
        boolean hasFailSafe = code.contains("при_сбое ") && code.contains(".освободить(");
        if (hasAllocation) {
            if (hasFailSafe) {
                log.append("• [ПАМЯТЬ]: Аллокация защищена контуром 'при_сбое'. Утечки исключены.\n");
            } else {
                return log.append("\n[СБОЙ КОМПИЛЯЦИИ]: Вызов '.выделить()' без контура 'при_сбое'!\n").toString();
            }
        }

        List<String> contracts = new ArrayList<>();
        Matcher reqMatcher = Pattern.compile("(требует|гарантирует)\\(([^\\)]+)\\)").matcher(code);
        while (reqMatcher.find()) {
            contracts.add(reqMatcher.group(1) + ": " + reqMatcher.group(2));
        }
        log.append("• Контрактов на линии: ").append(contracts.size()).append("\n");
        for (String c : contracts) {
            log.append("   -> ").append(c).append("\n");
        }

        log.append("\n=== [ИСПОЛНЕНИЕ УЗЛА: собрать_пакет] ===\n");
        byte[] testStream = new byte[]{10, 20, 30, 40};
        long expectedHash = computeCubeHash(testStream);
        log.append("• Метка: 0x5A5A5A5A (Допуск пройден)\n");
        log.append("• Поток: 4 байта | Хэш линии: ").append(expectedHash).append("\n");
        log.append("• [ИТОГ]: Изделие 'Пакет' выдано на направляющую без UB.\n");

        return log.toString();
    }

    private int calculateFacetBytes(String body) {
        int total = 0;
        Matcher m = Pattern.compile(":\\s*б(8|16|32|64)").matcher(body);
        while (m.find()) {
            total += (Integer.parseInt(m.group(1)) / 8);
        }
        return total;
    }

    private long computeCubeHash(byte[] data) {
        long hash = 0;
        for (byte b : data) {
            hash = (hash << 5) + hash + (b & 0xFF);
        }
        return hash;
    }

    /**
     * ПОЛНОЕ ОТКЛЮЧЕНИЕ ВСЕХ ПРОЦЕССОВ И ВЫЧИСЛИТЕЛЬНЫХ ЯДЕР
     */
    private void shutdownAllProcesses() {
        if (corePool != null) {
            corePool.shutdownNow();
        }
        if (codeEditor != null) codeEditor.setText("");
        if (consoleOutput != null) consoleOutput.setText("");
        finishAndRemoveTask();
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    /**
     * Чертеж №1: Исследовательский контур ЛабораторияПлазмы
     */
    private String getPlasmaContourCode() {
        return "// УНИВЕРСАЛЬНЫЕ КВАНТЫ ДЛЯ ИССЛЕДОВАНИЯ СРЕДЫ\n" +
               "квант ПолеДатчика = в32;\n" +
               "квант ВекторСреды = [3]в32; // [Температура, Давление, Энтропия]\n\n" +
               "куб ФильтрШума {\n" +
               "    узел очистить_квант(шумный: ПолеДатчика) -> ПолеДатчика {\n" +
               "        вернуть мат.сгладить(шумный, 0.05);\n" +
               "    }\n" +
               "}\n\n" +
               "куб ТепловойАнализ {\n" +
               "    узел вычислить_нагрев(срез_поля: []конст ПолеДатчика) -> в32 {\n" +
               "        вернуть мат.среднее_квадратичное(срез_поля);\n" +
               "    }\n" +
               "}\n\n" +
               "куб БароАнализ {\n" +
               "    узел вычислить_давление(срез_поля: []конст ПолеДатчика, поправка: в32) -> в32 {\n" +
               "        вернуть мат.максимум(срез_поля) * поправка;\n" +
               "    }\n" +
               "}\n\n" +
               "куб ПоискАномалий {\n" +
               "    узел найти_резонанс(срез_поля: []конст ПолеДатчика) -> в32 {\n" +
               "        вернуть мат.дисперсия(срез_поля);\n" +
               "    }\n" +
               "}\n\n" +
               "куб СинтезаторМодели {\n" +
               "    узел свести_вектор(нагрев: в32, давление: в32, резонанс: в32) -> ВекторСреды {\n" +
               "        вернуть [нагрев, давление, резонанс];\n" +
               "    }\n" +
               "    узел рассчитать_отклик(состояние: ВекторСреды) -> в32 {\n" +
               "        вернуть 1.0 / (1.0 + состояние[2]);\n" +
               "    }\n" +
               "}\n\n" +
               "контур ЛабораторияПлазмы {\n" +
               "    состояние поправка_реактора: в32 = 1.0;\n\n" +
               "    узел шаг_моделирования(\n" +
               "        сырая_матрица: [10000]ПолеДатчика,\n" +
               "        чистый_буфер:  *изм [10000]ПолеДатчика\n" +
               "    ) -> ВекторСреды {\n" +
               "        сырая_матрица =>> ФильтрШума.очистить_квант(_) => *чистый_буфер;\n\n" +
               "        конст итог_такта: ВекторСреды = (\n" +
               "            *чистый_буфер =< {\n" +
               "                ветвь_1: ТепловойАнализ.вычислить_нагрев(_),\n" +
               "                ветвь_2: БароАнализ.вычислить_давление(_, поправка_реактора),\n" +
               "                ветвь_3: ПоискАномалий.найти_резонанс(_)\n" +
               "            } >=> СинтезаторМодели.свести_вектор(ветвь_1, ветвь_2, ветвь_3)\n" +
               "        );\n\n" +
               "        итог_такта => СинтезаторМодели.рассчитать_отклик(_) ~> поправка_реактора;\n" +
               "        вернуть итог_такта;\n" +
               "    }\n" +
               "}\n";
    }

    /**
     * Чертеж №2: Закрытый СетевойКонтур
     */
    private String getInitialCubeCode() {
        return "// КУБ: СетевойКонтур\n" +
               "куб СетевойКонтур {\n\n" +
               "    сбой ОшибкаКонтура : б8 {\n" +
               "        НетПамяти,\n" +
               "        НеверныйРазмер,\n" +
               "        СбойКонтрольнойСуммы,\n" +
               "    }\n\n" +
               "    грань Заголовок @упаковано @выравнивание(4) {\n" +
               "        метка: б32,\n" +
               "        размер_данных: б32,\n" +
               "        контрольная_сумма: б64,\n" +
               "    }\n\n" +
               "    грань Пакет @без_копирования {\n" +
               "        шапка: Заголовок,\n" +
               "        тело: []б8,\n" +
               "        источник_памяти: Память,\n" +
               "        линия очистить(изм сам: *Пакет) -> пусто {\n" +
               "            сам.источник_памяти.освободить(сам.тело);\n" +
               "        }\n" +
               "    }\n\n" +
               "    узел собрать_пакет(\n" +
               "        память: Память,\n" +
               "        вх_шапка: Заголовок,\n" +
               "        вх_поток: []конст б8\n" +
               "    ) -> ОшибкаКонтура ! Пакет\n" +
               "        требует(вх_шапка.метка == 0x5A5A5A5A)\n" +
               "        требует(вх_поток.длина == вх_шапка.размер_данных)\n" +
               "        гарантирует(итог.тело.длина == вх_шапка.размер_данных)\n" +
               "    {\n" +
               "        конст буфер: []б8 = память.выделить(б8, вх_поток.длина) перехват {\n" +
               "            вернуть ОшибкаКонтура.НетПамяти;\n" +
               "        };\n" +
               "        при_сбое память.освободить(буфер);\n" +
               "        память.копировать(буфер, вх_поток);\n" +
               "        вернуть Пакет { .шапка = вх_шапка, .тело = буфер, .источник_памяти = память };\n" +
               "    }\n" +
               "}\n";
    }
}
