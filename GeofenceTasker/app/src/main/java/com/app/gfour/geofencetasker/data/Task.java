package com.app.gfour.geofencetasker.data;

/**
 * Bean class representing a task.
 */
public class Task {
    private int id;
    private String title;
    private String address;

    public Task(){}

    public Task(String title, String address) {
        super();
        this.title = title;
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", address=" + address
                + "]";
    }
}
