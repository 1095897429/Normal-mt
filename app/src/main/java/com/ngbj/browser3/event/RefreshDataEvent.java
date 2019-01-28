package com.ngbj.browser3.event;

public class RefreshDataEvent {
    private int index;
    public RefreshDataEvent(int index ){
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
