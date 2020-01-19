-module(task).
-export([start/1]).


start(Msgn) ->
    {ok, Context} = erlzmq:context(1),
    {ok, SockPush} = erlzmq:socket(Context, [push, {active, false}]),
    {ok, SockPub} = erlzmq:socket(Context, [pub, {active, false}]),
    ok = erlzmq:connect(SockPush, "tcp://localhost:12347"),
    ok = erlzmq:connect(SockPub, "tcp://localhost:12345"),
    io:format("ZMQ connect ~n"),
    erlzmq:send(SockPush, messages:encode_msg(Msgn,'Message')),
    Order = maps:get(manufacturerOrder,Msgn),
    Key = genKey(maps:get(id,Order)),
    handleRequest(SockPush,SockPub,Context,0,Key,Order).
    %io:format("Order Started ~n",[]),
    %handleRequest(0).

handleRequest(N) ->
    receive
        {offer,Msg,_} ->
            io:format("Offer recebida ~n",[]),
            N1 = N + 1,
            handleRequest(N1);
        {close,_} ->
            io:format("Order Completed ~n",[]) 
    end.


handleRequest(SockPush,SockPub,Context,N,Key,Order) ->
    receive
        {offer,Msg,Pid} ->
            N1 = N + 1,
            Offer = maps:get(importerOffer,Msg),
            Prod = maps:get(product,Order),
            Offer2 = maps:put(product,Prod,Offer),
            Offer3 = maps:put(state,'EMITTED',Offer2),
            case verifyprice(Order,Offer) of
                true ->
                    OfferReady = maps:update(id,N,Offer3),
                    MsgN = maps:update(importerOffer,OfferReady,Msg),
                    Env = build_envelope(Key,messages:encode_msg(MsgN,'Message')),
                    erlzmq:send(SockPub,Env),
                    io:format("Offer enviada ~n",[]),
                    handleRequest(SockPush,SockPub,Context,N1,Key,Order);
                _ ->
                    io:format("Offer não passa o valor min ~n",[]),
                    Pid ! {error,Offer2,self()},
                    handleRequest(SockPush,SockPub,Context,N,Key,Order)
            end;
        {close,_} ->
            erlzmq:close(SockPub),
            erlzmq:close(SockPub),
            erlzmq:term(Context),
            io:format("Order Completed ~n",[])    
    end.

verifyprice(Order,Offer) ->
    Min = maps:get(unitPrice,Order),
    Val = maps:get(unitPrice,Offer),
    Val >= Min.

genKey(Id) ->
    Str = string:concat(integer_to_list(Id),":"),
    list_to_binary(Str).

build_envelope(B1,B2) ->
    <<B1/binary, B2/binary>>.



