## SelectSort

```java
public class SelectSort {
    
    public static void main(String[] args) {
        int[] arr = new int[] { 1, 22, 4, 55, 7, 6, 1, 43, 21, 2, 5 };
        selectSort(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    private static void selectSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int min = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < arr[min]) {
                    min = j;
                }
            }
            if (i != min) {
                int temp = arr[i];
                arr[i] = arr[min];
                arr[min] = temp;
            }
        }
    }
}
```
