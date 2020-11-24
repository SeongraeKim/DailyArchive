package com.ksr.dailyarchive;

public class ListData implements Comparable<ListData>{

    private int tv_date;
    private String tv_content;

    public ListData(int tv_date, String tv_content){
        this.tv_date = tv_date;
        this.tv_content = tv_content;
    }

    public int getTv_date() {
        return tv_date;
    }

    public void setTv_date(int tv_date) {
        this.tv_date = tv_date;
    }

    public String getTv_content() {
        return tv_content;
    }

    public void setTv_content(String tv_content) {
        this.tv_content = tv_content;
    }

    @Override
    public int compareTo(ListData listData) {

        if(this.tv_date < listData.tv_date){
            return 1;
        }else if (this.tv_date == listData.tv_date){
            return 0;
        }else {
            return -1;
        }
    }
}