package core.main;

public interface MarkedUnit {
    public static final int MARK_NONE = 0,
                            MARK_NEEDS_PARTIAL_INFO = 1,
                            MARK_NEEDS_FULL_INFO = 2;

    public int getMark();
    public void setMark(int mark);
}
