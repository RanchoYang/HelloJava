## BubbleSort

```java
public class BubbleSort {

	public static void main(String[] args) {
		int[] arr = new int[] { 1, 22, 4, 55, 7, 6, 1, 43, 21, 2, 5 };
		bubbleSort(arr);
		for (int i = 0; i < arr.length; i++) {
			System.out.println(arr[i]);
		}
	}

	private static void bubbleSort(int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = i + 1; j < arr.length; j++) {
				if (arr[i] > arr[j]) {
					int temp = arr[j];
					arr[j] = arr[i];
					arr[i] = temp;
				}
			}
		}
	}
}
```
