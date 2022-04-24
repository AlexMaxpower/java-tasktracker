package tracker.controllers;

import tracker.model.Node;
import tracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private Map<Integer, Node> historyMap;
    private HistoryLinkedList<Task> historyLinkedList;

    public InMemoryHistoryManager() {
        historyMap = new HashMap<>();
        historyLinkedList = new HistoryLinkedList<>();
    }

    @Override
    public void add(Task task) {
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
                oldTail.setNext(newNode);
            }
            size++;

            return newNode;
        }

        public void removeNode(Node<T> element) {
            Node<T> prevElement;
            Node<T> nextElement;
            if (head != tail) {
                if (element == head) {
                   nextElement = element.getNext();
                   nextElement.setPrev(null);
                   head = nextElement;
                } else if (element == tail) {
                    prevElement = element.getPrev();
                    prevElement.setNext(null);
                    tail = prevElement;
                } else {
                    nextElement = element.getNext();
                    prevElement = element.getPrev();
                    nextElement.setPrev(prevElement);
                    prevElement.setNext(nextElement);
                }
            } else if (head == tail){
                head = null;
                tail = null;
            }
            size--;
        }

        public List<Task> getTasks() {
            List<Task> historyTasks = new ArrayList<>();
            Node<T> curNode = head;

            for (int i=0; i<size; i++) {
                historyTasks.add((Task) curNode.getData());
                Node<T> nextNode = curNode.getNext();
                curNode = nextNode;
            }
            return historyTasks;
        }
    }