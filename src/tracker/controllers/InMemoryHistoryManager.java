package tracker.controllers;

import tracker.model.Node;
import tracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private Map<Integer, Node> historyMap;
    private HistoryLinkedList<Task> historyLinkedList;
    private static final int MAX_VIEWS = 10;  // максимальное количество хранимых в истории задач

    public InMemoryHistoryManager() {
        historyMap = new HashMap<>();
        historyLinkedList = new HistoryLinkedList<>();
    }

    @Override
    public void add(Task task) {
        // если история просмотров заполнена - удаляем самую старую задачу
        if (historyLinkedList.size() >= MAX_VIEWS) {
            remove(historyLinkedList.getFirst().getTaskId());
        }

        if (historyMap.containsKey(task.getTaskId())) {
            Node<Task> deleteNode = historyMap.get(task.getTaskId());
            historyLinkedList.removeNode(deleteNode);
        }
            historyMap.put(task.getTaskId(),historyLinkedList.linkLast(task));
    }

    @Override
    public List<Task> getHistory(){
        return historyLinkedList.getTasks();
    }

    @Override
    public void remove(Integer id) {
        if (historyMap.containsKey(id)) {
            historyLinkedList.removeNode(historyMap.get(id));
            historyMap.remove(id);
        }
    }

}
    class HistoryLinkedList<T> {

        private Node<T> head;  // указатель на первый элемент связанного списка
        private Node<T> tail;  // указатель на последний элемент связанного списка

        private int size = 0;

        public T getFirst() {
            Node<T> curHead = head;
            if (curHead == null) {
                return null;
            }
            return head.data;
        }

        public Node<T> linkLast(T element) {
            Node<T> oldTail = tail;
            Node<T> newNode = new Node<>(tail, element, null);
            if (size == 0) {
                head = newNode;
            }
            tail = newNode;
            if (oldTail == null) {
                tail = newNode;
            }
            else {
                oldTail.next = newNode;
            }
            size++;

            return newNode;
        }

        public void removeNode(Node<T> element) {
            Node<T> prevElement;
            Node<T> nextElement;
            if (head != tail) {
                if (element == head) {
                   nextElement = element.next;
                   nextElement.prev = null;
                   head = nextElement;
                } else if (element == tail) {
                    prevElement = element.prev;
                    prevElement.next = null;
                    tail = prevElement;
                } else {
                    nextElement = element.next;
                    prevElement = element.prev;
                    nextElement.prev = prevElement;
                    prevElement.next = nextElement;
                }
            } else if (head == tail){
                head = null;
                tail = null;
            }
            size--;
        }

        public int size() {
            return this.size;
        }

        public List<Task> getTasks() {
            List<Task> historyTasks = new ArrayList<>();
            Node<T> curNode = head;

            for (int i=0; i<size; i++) {
                historyTasks.add((Task) curNode.data);
                Node<T> nextNode = curNode.next;
                curNode = nextNode;
            }
            return historyTasks;
        }
    }