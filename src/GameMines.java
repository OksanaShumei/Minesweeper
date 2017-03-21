import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by PC on 20.03.2017.
 */
public class GameMines extends JFrame{

    final String TITLE_OF_PROGRAM = "Game Mines";
    final String SIGN_OF_FLAG = "*";
    final int BLOCK_SIZE = 30;
    final int FIELD_SIZE = 9;
    final int FIELD_DX = 6;
    final int FIELD_DY = 28 + 17;
    final int START_LOCATION = 200;
    final int MOUSE_BUTTON_LEFT = 1;
    final int MOUSE_BUTTON_RIGHT = 3;
    final int NUMBER_OF_MINES = 10;
    final int[] COLOR_OF_NUMBERS ={0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0};
    Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE];
    Random random = new Random();
    int countOpenedCells;    //к-сть відкритих ячейок
    boolean youWon, bangMine;
    int bangX, bangY; //координати вибуху

    public static void main(String[] args) {
        new GameMines();
    }

    GameMines(){
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE * BLOCK_SIZE + FIELD_DX, FIELD_SIZE * BLOCK_SIZE + FIELD_DY);
        setResizable(false);
        TimerLabel timeLabel = new TimerLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Canvas canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.addMouseListener(new MouseAdapter() {
            @Override                   //MouseEvent е - яка клавіша мишки була задіяна коли був клік
            public void mouseReleased(MouseEvent e) { //переоприділяємо метод mouseReleased
                super.mouseReleased(e); // викликаємо метод mouseReleased у батьківського класу
                int x = e.getX()/BLOCK_SIZE; // звертаємося до змінної е і викликаємо методи, ми отримуємо абсолютні координати кліку мишки,
                int y = e.getY()/BLOCK_SIZE;// ділимо на розмір блоку і отримуємо відносні координати(по якій клітці нашого поля ми клікнули в даний момент)
                if (!bangMine && !youWon) {
                    if (e.getButton() == MOUSE_BUTTON_LEFT) // left button mouse
                        if (field[y][x].isNotOpen()) {
                            openCells(x, y);
                            youWon = countOpenedCells == FIELD_SIZE*FIELD_SIZE - NUMBER_OF_MINES; // winning check
                            if (bangMine) {
                                bangX = x;
                                bangY = y;
                            }
                        }
                    if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag(); // right button mouse
                    if (bangMine || youWon) timeLabel.stopTimer(); // game over
                    canvas.repaint();
                }
            }
        });

        add(BorderLayout.CENTER, canvas);//ставиться в центр екрану
        add(BorderLayout.SOUTH, timeLabel);
        setVisible(true);//робить вікно видимим
        initField();//виконується код методу initField();
    }

    void openCells(int x, int y){
        if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE - 1) return; // wrong coordinates
        if (!field[y][x].isNotOpen()) return; // cell is already open
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || bangMine) return; // the cell is not empty
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) openCells(x + dx, y + dy);
    }

    class Cell {

        private boolean isOpen, isMine, isFlag;
        private int countBombNear;

        void open() {
            isOpen = true;
            bangMine = isMine;
            if (!isMine) countOpenedCells++;
        }

        void mine() {
            isMine = true;//ячейка мінується
        }

        boolean isNotOpen() {
            return !isOpen;
        }//перевіряється відкрита ячецка чи ні

        void inverseFlag() {
            isFlag = !isFlag;
        }//інвертування флагу

        boolean isMined() {
            return isMine;
        }//чи замінована ячейка

        void setCountBomb(int count) {//установлює к-мть бомб
            countBombNear = count;
        }

        int getCountBomb() {
            return countBombNear;
        }

        void paintBomb(Graphics g, int x, int y, Color color) {//намалювати бомбу
            g.setColor(color);
            g.fillRect(x*BLOCK_SIZE + 7, y*BLOCK_SIZE + 10, 18, 10);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 6, 10, 18);
            g.fillRect(x*BLOCK_SIZE + 9, y*BLOCK_SIZE + 8, 14, 14);
            g.setColor(Color.white);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 10, 4, 4);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) {//відображати к-сть цифр в ячейці
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, BLOCK_SIZE));
            g.drawString(str, x*BLOCK_SIZE + 8, y*BLOCK_SIZE + 26);
        }


        void paint(Graphics g, int x, int y) {
            g.setColor(Color.lightGray);                                             //малює прямокутник і робить розмітку
            g.drawRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE); // у вигляді маленьких квадратиків
            if (!isOpen) {//якщо ячейка не відкрита
                if ((bangMine || youWon) && isMine) paintBomb(g, x, y, Color.black);
                else {
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
                    if (isFlag) paintString(g, SIGN_OF_FLAG, x, y, Color.red);//якщо флажок всановлений то малюється *
                }
            } else if (isMine) paintBomb(g, x, y, bangMine ? Color.red : Color.black);
            else if (countBombNear > 0)
                paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
        }
    }

    void initField() {

        // initialization of the playing field
        int x, y, countMines = 0;

        // create cells for the field
        // створюється шгрове поле і заповлюється обєктами
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                field[y][x] = new Cell();

        // to mine field
        // генерує міни, знаходиться в цьому об"єкті міна чи ні
        // рандомно вибираються координати, але потім перевіряємо якщо значення = true,
        // то цикл повторюється до тих пір поки результат методу isMined не буде = false,
        // це означає що ячейка не замінована, і тоді ячейка мінується за допомогою методу mine
        while (countMines < NUMBER_OF_MINES) {
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x].isMined());
            field[y][x].mine();//ячейка мінується і к-сть мін збільшується на 1,
            countMines++;     // спочатку він рівнуй 0
        }

        // to count dangerous neighbors
        // рахує міни навколо
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for (int dx = -1; dx < 2; dx++)
                        for (int dy = -1; dy < 2; dy++) {
                            int nX = x + dx;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1) {
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                    field[y][x].setCountBomb(count);//вираховує к-ть сусідніх мін у даного об"єкту
                }
    }

    class TimerLabel extends JLabel { // label with stopwatch
        Timer timer = new Timer();

        TimerLabel() { timer.scheduleAtFixedRate(timerTask, 0, 1000); } // TimerTask task, long delay, long period

        TimerTask timerTask = new TimerTask() {
            volatile int time;
            Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };
            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        void stopTimer() { timer.cancel(); }
    }

    class Canvas extends JPanel{

        @Override
        public void paint(Graphics g) {
            super.paint(g);//викликаємо батьківський метод отрисовки
            for (int x = 0; x < FIELD_SIZE; x++)
                for (int y = 0; y < FIELD_SIZE; y++) field[y][x].paint(g, x, y);// викликаємо метод отрисовки
        }

    }
}
