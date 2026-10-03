# A simple ant simulation in Java. Still a WIP.

How it's meant to work:
1. The ants should be able to "see" the cells in front of them to determine whether they contain food or pheromones.
2. If the ant doesn't have food, it should look for food. If no food is found, look for red "to-Food" pheromones. If neither found, wander.
3. If the ant has food, it should look for the home cell. If home isn't found, look for green "to-Home" pheromones. If neither found, wander.
4. Finally, when an ant with food reaches the home cell, add to total food collected and repeat.

Ideally, this should result in the ants forming efficient routes to their food sources, a form of of emergent behaviour.

Project inspired by [Sebastian Lague's](https://github.com/seblague) ant and slime video on Youtube, found [here](https://www.youtube.com/watch?v=X-iSQQgOd1A&t=392s),  and [Pezzo's](https://github.com/johnBuffer/AntSimulator) C++ simulation.

<img width="706" height="680" alt="image" src="https://github.com/user-attachments/assets/1f574728-b850-446d-a29c-c6f91e85508b" />

