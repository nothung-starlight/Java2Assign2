package my;

import java.net.Socket;

public class match {
    int[][] match;
    int turn;
    String id1;
    String id2;
    String winner;
    Socket client1;
    Socket client2;

    public match(String id1, String id2, Socket client1, Socket client2) {
        match = new int[3][3];
        turn = 1;
        this.id1 = id1;
        this.id2 = id2;
        this.client1 = client1;
        this.client2 = client2;
        winner = null;
    }

    public void end() {
        boolean k = false;
        for (int[] i : match) {
            for (int j : i) {
                if (j == 0) {
                    k = true;
                    break;
                }
            }
        }
        if (!k) {
            winner = "0";
        }
        for (int i = 0; i < 3; i++) {
            if (match[i][0] == match[i][1] && match[i][1] == match[i][2] && match[i][1] != 0) {
                if (match[i][1] == 1) {
                    winner = id1;
                }
                if (match[i][1] == -1) {
                    winner = id2;
                }

            } else if (match[0][i] == match[1][i] && match[1][i] == match[2][i] && match[1][i] != 0) {
                if (match[0][i] == 1) {
                    winner = id1;
                }
                if (match[0][i] == -1) {
                    winner = id2;
                }

            }
        }
        if (match[0][0] == match[1][1] && match[2][2] == match[1][1] && match[2][2] != 0) {
            if (match[1][1] == 1) {
                winner = id1;
            }
            if (match[1][1] == -1) {
                winner = id2;
            }

        } else if (match[0][2] == match[1][1] && match[2][0] == match[1][1] && match[2][0] != 0) {
            if (match[1][1] == 1) {
                winner = id1;
            }
            if (match[1][1] == -1) {
                winner = id2;
            }

        }
    }

    public boolean update(int x, int y, int z) {
        if (winner == null) {
            if (turn == z && match[x][y] == 0) {

                match[x][y] = z;
                turn = -turn;
                return true;
            }
        }
        return false;
    }
}
