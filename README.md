# TaskTracker – бекэнд менеджера задач
Программа позволяет ставить цели, задачи и сроки по проектам, следить и
измерять активность.

Задачи могут быть трёх типов: обычные задачи, эпики и подзадачи.
## Реализованные функции:
* создание задач разных типов с учетом указанного в них времени выполнения (задачи и подзадачи не могут пересекаться)
* получение списка всех задач определенного типа
* получение задачи по id
* удаление задачи по id
* удаление всех задач определённого типа
* обновление задач
* возвращение всех подзадач определённого эпика
* определение статуса эпиков исходя из статусов подзадач
* возвращение истории запрашиваемых задач
* запись данных в файл
* восстановление менеджера задач из файла
* хранение задач в памяти, файле, на сервере
* доступ к методам менеджера через HTTP-запросы
