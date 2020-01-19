-module(catalogManager).
-export([start/0]).


start() ->
    register(?MODULE, spawn(fun() -> io:format("New user logged in ~n",[]) end)).


