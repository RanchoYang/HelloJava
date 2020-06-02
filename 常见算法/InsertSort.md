## InsertSort

```java
public class InsertSort {

    public static void main(String[] args) {
        int[] arr = new int[] { 1, 22, 4, 55, 7, 6, 1, 43, 21, 2, 5 };
        insertSort(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    private static void insertSort(int[] arr) {

        for (int i = 1; i < arr.length; i++) {
            int temp = arr[i];
            int index = i;
            // 从右向左找到小于当前值的位置并插入
            while (temp < arr[index - 1] && index > 0) {
                arr[index] = arr[index - 1];
                index--;
            }
            arr[index] = temp;
        }
    }
}
```