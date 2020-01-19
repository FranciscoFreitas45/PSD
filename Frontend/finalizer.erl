-module(finalizer).
-export([start/0]).

start() ->
    {ok, Context} = erlzmq:context(1),
    {ok, SockPull} = erlzmq:socket(Context, [pull, {active, false}]),
    ok = erlzmq:bind(SockPull,"tcp://*:12349"),
    register(?MODULE, spawn(fun() -> finalize(SockPull) end)).


finalize(Sock) ->
    case erlzmq:recv(Sock) of
        {ok, Bin} ->
            io:format("Reply arrived ~n",[]),
            Msg = messages:decode_msg(Bin,'Message'),
            Reply = maps:get(reply,Msg),
            IdOrder = maps:get(id,Reply),
            PidOrder = taskManager:lookup(IdOrder), % pid com a task
            PidOrder ! {close,self()}, % acabar com a task;
            Userbids = maps:get(offers,Reply),
            sendReplies(Userbids,maps:get(res,Reply)),
            sendReplyManu(Reply),
            finalize(Sock);
        
        {error, _} ->
            finalize(Sock)
    end.


sendReplyManu(Reply) ->
    Uname = maps:get(manufacturer,Reply),
    Upid = loginManager:lookUp(Uname),
    Upid ! {corder,Reply,self()}.

sendReplies([],_) -> ok;
sendReplies([H|T],Status) ->
    Uname = maps:get(importer,H),
    Upid = loginManager:lookUp(Uname),
    Upid ! {reply,Status,H,self()},
    sendReplies(T,Status).