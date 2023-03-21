package jp.moreslowly.oi.models;

import java.util.Arrays;
import java.util.List;

public final class Nickname {
  public static final List<String> NICKNAME_LIST = Arrays.asList(
    "サクラ",
    "ウメ",
    "カエデ",
    "シラカバ",
    "アジサイ",
    "ツツジ",
    "モミジ",
    "ツバキ",
    "マツ",
    "モクレン",
    "タケ",
    "ヒイラギ"
  );
  public static final int NICKNAME_LIST_SIZE = NICKNAME_LIST.size();

  private Nickname() {
  }
}
