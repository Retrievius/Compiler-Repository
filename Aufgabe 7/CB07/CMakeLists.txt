cmake_minimum_required(VERSION 4.0)
project(CB07)

set(CMAKE_CXX_STANDARD 20)

include_directories(include)

add_executable(CB07
        src/main.cpp
        src/Token.cpp
        src/Tokenizer.cpp
        src/RefCounter.cpp
        src/SmartToken.cpp
        src/RingBuffer.cpp
)