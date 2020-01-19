-module(loginManager).
-export([start/0,reply/0,createAccount/3,logIn/2,logOut/1,lookUp/1]).


start() ->
    register(?MODULE, spawn(fun() -> manage(#{}) end)).


createAccount(User,Pass,Type) ->
    ?MODULE ! {create,User,Pass,Type,self()},
    reply().

logIn(User,Pass) ->
    ?MODULE ! {login,User,Pass,self()},
    reply().

logOut(User) ->
    ?MODULE ! {logout,User,self()}.

lookUp(User) ->
    ?MODULE ! {lookup,User,self()},
    receive
        {lookup,PidResult,_} -> 
            PidResult
    end.

manage(Map) ->
    receive
        {create,User,Pass,Type,Pid} ->
            case maps:find(User,Map) of
                error ->
                    Pid ! {?MODULE,signedup},
                    io:format("New user ~n",[]),
                    manage(maps:put(User,{Pass,false,Type},Map));
                _ -> 
                    Pid ! {?MODULE,fail},
                    manage(Map)
            end;
        {login,User,Pass,Pid} ->
            case maps:find(User,Map) of
                {ok,{Pass,_,Type}} ->
                    Pid ! {?MODULE,{loggedIn,Type}},
                    io:format("New user logged in ~n",[]),
                    manage(maps:put(User,{Pass,Pid,Type},Map));
                _ ->
                    Pid ! {?MODULE,error},
                    manage(Map)
            end;
        {logout,User,_} ->
            case maps:find(User,Map) of
                {ok,{Pass,_,Type}} ->
                    manage(maps:put(User,{Pass,false,Type},Map));
                _ ->
                    manage(Map)
            end;
        {lookup,User,Pid} ->
            {_,Upid,_} = maps:get(User,Map),
            Pid ! {lookup,Upid,self()}
    end.

reply()->
    receive
        {?MODULE,Rep} -> Rep
    end.