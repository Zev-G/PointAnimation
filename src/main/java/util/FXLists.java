package util;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.function.Function;

public final class FXLists {

    public static <I, F> ObservableList<F> map(ObservableList<I> initial, Function<I, F> convert) {
        ObservableList<F> result = FXCollections.observableList(convert(initial, convert));
        initial.addListener((ListChangeListener<I>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    int start = c.getFrom();
                    int end = c.getTo();
                    for (int oldIndex = start; oldIndex < end; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        Collections.swap(result, oldIndex, newIndex);
                    }
                } else if (c.wasUpdated()) {
                    int start = c.getFrom();
                    int end = c.getTo();
                    for (int i = start; i < end; i++) {
                        result.set(i, convert.apply(c.getList().get(i)));
                    }
                } else {
                    result.removeAll(convert(c.getRemoved(), convert));
                    result.addAll(convert(c.getAddedSubList(), convert));
                } }
        });
        return result;
    }

    public static <T> ObservableList<T> reduce(ObservableList<ObservableList<T>> initial) {
        ObservableList<T> reduced = FXCollections.observableArrayList();

        class Link {
            final ObservableList<T> value;
            Link previous;
            Link next;

            final ListChangeListener<? super T> listener;

            int start = 0;

            Link(ObservableList<T> value, Link previous, Link next) {
                this.value = value;
                this.previous = previous;
                this.next = next;

                if (previous != null) { setStart(previous.start + previous.value.size()); }

                reduced.addAll(start, value);
                value.addListener(listener = change -> {
                    if (next != null) {
                        next.setStart(start + value.size());
                    }
                    while (change.next()) {
                        if (change.wasPermutated()) {
                            int from = change.getFrom();
                            int to = change.getTo();
                            for (int i = to; i < from; i++) {
                                Collections.swap(reduced, i, change.getPermutation(i));
                            }
                        } else {
                            if (change.wasAdded()) {
                                reduced.addAll(start, change.getAddedSubList());
                            }
                            reduced.removeAll(change.getRemoved());
                        }
                    }
                });
            }

            void detach() {
                value.removeListener(listener);
                reduced.remove(start, start + value.size());

                if (next != null && previous != null) {
                    this.previous.setNext(next);
                    this.next.setPrevious(previous);
                }
                if (next == null && previous != null) {
                    this.previous.setNext(null);
                }
                if (previous == null && next != null) {
                    this.next.setPrevious(null);
                }

                if (next != null) {
                    next.setStart(start);
                }
            }

            void setStart(int start) {
                if (this.start != start) {
                    this.start = start;
                    if (next != null) {
                        next.setStart(this.start + value.size());
                    }
                }
            }

            void setNext(Link next) {
                if (this.next != null && this.next.previous == this) {
                    this.next.previous = null;
                }
                this.next = next;
            }
            void setPrevious(Link previous) {
                if (this.previous != null && this.previous.next == this) {
                    this.previous.next = null;
                }
                this.previous = previous;
            }

        }

        Map<ObservableList<T>, Link> listLinkMap = new HashMap<>();

        Link previous = null;
        for (ObservableList<T> list : initial) {
            Link oldPrevious = previous;
            previous = new Link(list, previous, null);
            listLinkMap.put(list, previous);
            if (oldPrevious != null) {
                oldPrevious.next = previous;
            }
        }

        List<ObservableList<T>> loaded = new ArrayList<>(initial);
        initial.addListener((ListChangeListener<? super ObservableList<T>>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    int from = c.getFrom();
                    int to = c.getTo();
                    for (int i = to; i < from; i++) {
                        int permutation = c.getPermutation(i);
                        if (permutation != i) {
                            Link link = listLinkMap.get(loaded.get(i));
                            if (i != loaded.size() + 1) {
                                Link next = listLinkMap.get(loaded.get(i + 1));
                                link.setNext(next);
                            }
                            if (i != 0) {
                                Link previousL = listLinkMap.get(loaded.get(i - 1));
                                link.setPrevious(previousL);
                            }
                            Collections.swap(loaded, i, permutation);
                        }
                    }
                } else {
                    if (c.wasAdded()) {
                        for (ObservableList<T> added : c.getAddedSubList()) {
                            Link newLink = new Link(added, null, null);
                            if (!loaded.isEmpty()) {
                                Link last = listLinkMap.get(loaded.get(loaded.size() - 1));
                                if (last != null) {
                                    newLink.setPrevious(last);
                                    last.setNext(newLink);
                                }
                            }
                            loaded.add(added);
                        }
                    }
                    if (c.wasRemoved()) {
                        for (ObservableList<T> removed : c.getRemoved()) {
                            Link link = listLinkMap.get(removed);
                            if (link != null) {
                                link.detach();
                            }
                            loaded.remove(removed);
                        }
                    }
                }
            }
        });

        return reduced;
    }

    private static <I,F> List<F> convert(List<I> initial, Function<? super I, F> convert) {
        List<F> result = new ArrayList<>(initial.size());
        for (I value : initial) {
            result.add(convert.apply(value));
        }
        return result;
    }

}
