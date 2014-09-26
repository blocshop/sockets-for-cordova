package cz.blocshop.socketsforcordova;

public interface Consumer<T> {
    void accept(T t);
}