package com.hwatong.f70.observable;

public interface Function<Result, Param> {

  Result function(Param... data);
}
