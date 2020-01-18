-module(userpainel).
-export([start/2]).


start(Sock,Name) ->
    receive
        {tcp, Sock, Bin} ->
            Msg = messages:decode_msg(Bin,'Message'),
            case maps:get(type,Msg) of
                "OFFER" ->
                    Offer = maps:get(importerOffer,Msg),
                    IdOrder = maps:get(idorder,Offer),
                    {_,PidWorkerFE} = taskManager:lookup(IdOrder),
                    PidWorkerFE ! {offer,Msg,self()},
                    start(Sock,Name);
                "ORDER" ->
                    taskManager:sendOrder(Msg),
                    start(Sock,Name)
            end;
        {tcp_closed, _} ->
			loginManager:logOut(Name)
    end.