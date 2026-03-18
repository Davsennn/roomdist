# roomdist Application
Divide many people into groups of specified sizes with preferences and weights.

## Problem
Current solution: Brute-Force everything. </br>
Better: catch `Double.NEGATIVE_INFINITY` results for individual rooms, immediately avoid exploring any configs with that room.
</br>
TODO: 
- Fix `chooseRoom` to reset for each chonfig.
- Find better way to generate possible groupings. THEN:
  - Early prune `Double.NEGATIVE_INFINITY` room configs
  - Prune differently-ordered but already explored paths (Search unordered, maybe use HashSet)
