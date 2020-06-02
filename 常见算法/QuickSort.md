## QuickSort

```java
public class QuickSort {
    public static void main(String[] args) {
        int[] arr = new int[] { 1, 22, 4, 55, 7, 6, 1, 43, 21, 2, 5 };
        quickSort(arr, 0, arr.length - 1);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    private static void quickSort(int[] arr, int left, int right) {
        if (left >= right) {
            return;
        }
        int low = left;
        int high = right;
        // 设置数组最后一个值为基准值
        int pivot = arr[high];
        while (low < high) {
            // 这里比较的时候要使用<=防止数组里面出现重复的值，否则会出现死循环
            while (low < high && arr[low] <= pivot) {
                // 从数组左侧开始找大于基准值的值放到右边去
                low++;
            }
            arr[high] = arr[low];
            while (low < high && arr[high] >= pivot) {
                // 从数组右侧开始找小于基准值的值放到左边去
                high--;
            }
            arr[low] = arr[high];
        }
        // 此时下标low=high
        // 把基准值放到数组中间
        arr[high] = pivot;
        // 递归排序基准值左侧的数组
        quickSort(arr, left, high - 1);
        // 递归排序基准值右侧的数组
        quickSort(arr, high + 1, right);
    }
}
```
