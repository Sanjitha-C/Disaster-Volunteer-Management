# Stage 9 cyclomatic complexity

Measured 2026-10-30 on `backend/src/main/java` with Lizard 1.24.3 using:

```cmd
python -m pip install -r testing\complexity\requirements.txt
python -m lizard -l java -C 10 backend\src\main\java
```

Lizard analyzed 78 functions. Average cyclomatic complexity (CCN) is 1.9; two functions exceed the configured threshold of 10:

| Function | File | CCN |
|---|---|---:|
| `AdminManagementService.updateAccess` | `backend/src/main/java/edu/dvc/admin/AdminManagementService.java` | 11 |
| `OperationsService.updateTaskStatus` | `backend/src/main/java/edu/dvc/operations/OperationsService.java` | 13 |

These two methods are refactoring candidates. All remaining functions have CCN of 10 or lower. Lizard exits with a warning status when it finds threshold violations; its measured output is an analysis result, not a test/build failure.
