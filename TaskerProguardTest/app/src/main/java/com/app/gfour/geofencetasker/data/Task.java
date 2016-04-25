package com.app.gfour.geofencetasker.data;

import anotherPackage.TasksActivity;
/**
 * Bean class representing a task.
 */
public class Task {
    private int id;
    private String title;
    private String address;

    public Task() {
        TasksActivity ta = new TasksActivity();
        com.app.gfour.geofencetasker.newtask.TasksActivity tb = new com.app.gfour.geofencetasker.newtask.TasksActivity();
    }
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
        return "Task [id=" + id + ", title=" + title + ", address=" + address
                + "]";
    }
}
