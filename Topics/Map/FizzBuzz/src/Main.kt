fun iterator(map: Map<String, Int>) {
    for (map in map) {
        if (map.value % 3 == 0)
            println("Fizz")
        else if (map.value % 5 == 0)
            println("Buzz")
        else
            println("FizzBuzz")
    }
}