-module(userpainel).
-export([start/2]).


start(Sock,Name) ->
    receive
        {tcp, Sock, Bin} ->
            Msg = messages:decode_msg(Bin,'Message'),
            case maps:get(type,Msg) of
                "OFFER" ->
                    start(Sock,Name);
                "ORDER" ->
                    taskManager:sendOrder(Msg),
                    start(Sock,Name)
            end;
        {tcp_closed, _} ->
			loginManager:logout(Name)

    end.