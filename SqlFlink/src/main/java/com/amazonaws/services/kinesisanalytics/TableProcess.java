package com.amazonaws.services.kinesisanalytics;

public class TableProcess {

    private String src_field_name;
    private String field_type;


    public TableProcess() {
    }

    public TableProcess(String src_field_name, String field_type) {
        this.src_field_name = src_field_name;
        this.field_type = field_type;

    }

    public TableProcess(Integer id, String src_field_name, String field_name, String field_type, String first_record, String updated_date) {
        this.src_field_name = src_field_name;
        this.field_type = field_type;
    }


    public String getSrc_field_name() {
        return src_field_name;
    }

    public void setSrc_field_name(String src_field_name) {
        this.src_field_name = src_field_name;
    }

    public String getField_type() {
        return field_type;
    }

    public void setField_type(String field_type) {
        this.field_type = field_type;
    }

    @Override
    public String toString() {
        return "TableProcess{" +
                "src_field_name='" + src_field_name + '\'' +
                ", field_type='" + field_type + '\'' +
                '}';
    }
}