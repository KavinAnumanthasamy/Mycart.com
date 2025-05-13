package com.mycart.mycart.Model;

public class Category {

    private String categoryId;
    private String categoryName;
    private String department;

    public Category() {}

    public Category(String categoryId, String categoryName, String department) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.department = department;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getdepartment() {
        return department;
    }

    public void setdepartment(String department) {
        this.department = department;
    }
}
