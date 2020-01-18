-module(task).
-export([start/1]).


start(Msgn) ->
    %{ok, Context} = erlzmq:context(1),
    %{ok, SockPush} = erlzmq:socket(Context, [push, {active, false}]),
    %{ok, SockPub} = erlzmq:socket(Context, [pub, {active, false}]),
    %ok = erlzmq:connect(SockPush, "tcp://localhost:12347"),
    %ok = erlzmq:connect(SockPub, "tcp://localhost:12345"),
    %io:format("ZMQ connect "),
    %erlzmq:send(SockPush, messages:encode_msg(Msgn)),
    %Order = maps:get(manufacturerOrder,Msgn),
    %Key = genKey(maps:get(id,Order),(maps:get(product,Order))),
    %handleRequest(SockPush,SockPub,Context,0,Key).
    io:format("Order Started ~n",[]),
    handleRequest(0).

handleRequest(N) ->
    receive
        {offer,Msg,_} ->
            io:format("Offer recebida ~n",[]),
            N1 = N + 1,
            handleRequest(N1);
        {close,_} ->
            io:format("Order Completed ~n",[]) 
    end.


handleRequest(SockPush,SockPub,Context,N,Key) ->
    receive
        {offer,Msg,_} ->
            N1 = N + 1,
            Offer = maps:get(importerOffer,Msg),
            OfferReady = maps:update(id,N,Offer),
            MsgN = maps:update(importerOffer,OfferReady,Msg),
            Env = build_envelope(Key,messages:encode_msg(MsgN)),
            erlzmq:send(SockPub,Env),
            io:format("Offer enviada ~n",[]),
            handleRequest(SockPush,SockPub,Context,N1,Key);
        {close,_} ->
            erlzmq:close(SockPub),
            erlzmq:close(SockPub),
            erlzmq:term(Context),
            io:format("Order Completed ~n",[])    
    end.


genKey(Id,Name) ->
    Str = string:concat(integer_to_list(Id),Name),
    list_to_binary(Str).

build_envelope(B1,B2) ->
    <<B1/binary, B2/binary>>.



