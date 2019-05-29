package com.chensi.box.data;

import android.content.SharedPreferences;

public class BigNumber {
    // Controll big number.
    // Don't allow minus number.

    private static final int MAX_LEVEL = 4;
    private static final int MAX_VALUE = 1000000;

    private static final String[] NUMBER_GRADES = {"k", "M", "G", "T", "P", "E", "Z", "Y"};

    public int value[] = new int[MAX_LEVEL + 1];  // Don't use last index. It will avoid overflow.

    public BigNumber() {
        init();
    }

    private void init() {
        for (int i = 0; i < MAX_LEVEL; i++) {
            value[i] = 0;
        }
    }


    @Override
    public String toString() {
        for (int i = MAX_LEVEL - 1; i > 0; i--) {
            if (value[i] >= 1000) {
                return ((float)value[i]) / 1000 + NUMBER_GRADES[i * 2 + 1];
            } else if (value[i] > 0) {
                return ((float)value[i]) / 1000 + NUMBER_GRADES[i * 2];
            }
        }
        if (value[0] < 1000) return value[0] + "";
        return ((float) value[0]) / 1000 + NUMBER_GRADES[0];
    }

    // ***** Caculate *****
    public BigNumber add(BigNumber big) {
        for (int i = 0; i < MAX_LEVEL; i++) {
            value[i] += big.value[i];
        }
        rising();

        return this;
    }

    public BigNumber add(int val) {
        value[0] += val % MAX_VALUE;
        value[1] += val / MAX_VALUE;
        rising();

        return this;
    }

    private void rising() {
        for (int i = 0; i < MAX_LEVEL; i++) {
            if (value[i] > MAX_VALUE) {
                value[i + 1] = value[i] / MAX_VALUE;
                value[i] = value[i] % MAX_VALUE;
            }
        }
    }

    public BigNumber sub(BigNumber big) {
        for (int i = MAX_LEVEL - 1; i >= 0; i--) {
            value[i] -= big.value[i];
        }
        dropping();

        return this;
    }

    public BigNumber sub(int val) {
        value[0] -= val % MAX_VALUE;
        value[1] -= val / MAX_VALUE;
        dropping();

        return this;
    }

    private void dropping() {
        for (int i = 0; i < MAX_LEVEL; i++) {
            if (value[i] < 0) {
                boolean success = false;
                for (int j = i + 1; j < MAX_LEVEL; j++) {
                    if (value[j] > 0) {
                        value[j]--;
                        success = true;
                        break;
                    } else {
                        value[j] += MAX_VALUE - 1;
                    }
                }
                if (!success) {
                    // fail. number is munus.
                    init();     // reset number to 0.
                }
                value[i] += MAX_VALUE;
            }
        }
    }

    public BigNumber set(int val) {
        init();
        add(val);
        return this;
    }

    public int biggerThen(int val) {
        for (int i = MAX_LEVEL - 1; i >= 0; i--) {
            if (value[i] > 0) {
                int valComp = val;
                for (int j = 0; j < i; j++) {
                    valComp /= MAX_VALUE;
                }
                if (value[i] > valComp) {
                    return 1;
                } else if (value[i] < valComp) {
                    return -1;
                }
            }
        }

        return 0;
    }

    public int biggerThen(BigNumber big) {
        for (int i = MAX_LEVEL - 1; i >= 0; i--) {
            if (value[i] > big.value[i]) {
                return 1;
            } else if (value[i] < big.value[i]) {
                return -1;
            }
        }

        return 0;
    }

    // ***** Utility *****
    public void getFromSPref(SharedPreferences pref, String key, int defValue) {
        if (pref.contains(key)) {
            for (int i = 0; i < MAX_LEVEL; i++) {
                value[i] = pref.getInt(key + "%" + i, 0);
            }
        } else {
            init();
            add(defValue);
        }
    }

    public void putToSPref(SharedPreferences.Editor editor, String key) {
        editor.putInt(key, 1);
        for (int i = 0; i < MAX_LEVEL; i++) {
            editor.putInt(key + "%" + i, value[i]);
        }
    }

}
