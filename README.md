GroupMessenger
==============

Totally and Causally Ordered Group Messenger with a Local Persistent Key-Value Table



- The app multicasts every user-entered message to all app instances (including the one that is sending the message). In the rest of the description, “multicast” always means sending a message to all app instances.

- App uses B-multicast. It does not implement R-multicast.

- A content provider is implemented using SQLite on Android to store pair.

- We have fixed the ports & sockets.
    - App opens one server socket that listens on 10000.
    - The grading uses 5 AVDs. The redirection ports are 11108, 11112, 11116, 11120, and 11124.

- Every message is stored in the provider individually by all app instances. Each message is stored as a pair. The key should be the final delivery sequence number for the message (as a string); the value should be the actual message (again, as a string). The delivery sequence number should start from 0 and increase by 1 for each message.
