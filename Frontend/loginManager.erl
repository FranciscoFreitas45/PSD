-module(loginManager).
-export([start/0,reply/0,createAccount/3,logIn/2,logOut/1]).


start() ->
    register(?MODULE, spawn(fun() -> manage(#{}) end)).


createAccount(User,Pass,Type) ->
    ?MODULE ! {create,User,Pass,Type,self()},
    reply().

logIn(User,Pass) ->
    ?MODULE ! {login,User,Pass,self()},
    reply().

logOut(User) ->
    ?MODULE ! {logout,User,self()},
    reply().

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
        {logout,User,Pid} ->
            case maps:find(User,Map) of
                {ok,{Pass,_,Type}} ->
                    Pid ! {?MODULE,{loggedOut,Type}},
                    manage(maps:put(User,{Pass,false,Type},Map));
                _ ->
                    Pid ! {?MODULE,error},
                    manage(Map)
            end
    end.

reply()->
    receive
        {?MODULE,Rep} -> Rep
    end.