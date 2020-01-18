-module(frontend).
-export([server/1]).


server(Port) ->
    loginManager:start(),
    {ok, LSock} = gen_tcp:listen(Port, [binary,{packet, 0}, {reuseaddr, true}, {active, true}]),
    acceptor(LSock).


acceptor(LSock) ->
    {ok, Sock} = gen_tcp:accept(LSock),
    spawn(fun() -> acceptor(LSock) end),
    authenticate(Sock).

authenticate(Sock) ->
    receive
        {tcp, Sock, Bin} ->
            Msg = messages:decode_msg(Bin,'Message'),
            User = maps:get(user,Msg),
            Name = maps:get(username,User),
            Pass = maps:get(password,User),
            case maps:get(type,Msg) of
                "REGISTER" ->
                    Type = maps:get(type,User),
                    case loginManager:createAccount(Name,Pass,Type) of
                        {signedup,Type} ->
                            ReplyBin = messages:encode_msg(#{type => "RESPONSE", response => #{status => 1, response => "REGISTED"}}, 'Message'),
                            gen_tcp:send(Sock, ReplyBin),
                            authenticate(Sock);
                        fail ->
                            ReplyBin = messages:encode_msg(#{type => "RESPONSE", response => #{status => 0, response => "USER NAME ALREADY USED"}}, 'Message'),
                            gen_tcp:send(Sock, ReplyBin),
                            authenticate(Sock)
                    end;
                "LOGIN" ->
                    case loginManager:logIn(Name,Pass) of
                        {loggedIn,Type} ->
                            ReplyBin = messages:encode_msg(#{type => "RESPONSE", response => #{status => 1, response => integer_to_list(Type)}}, 'Message'),
                            gen_tcp:send(Sock, ReplyBin),
                            userpainel:start(Sock,Name);
                        error -> 
                            ReplyBin = messages:encode_msg(#{type => "RESPONSE", response => #{status => 0, response => "CREDENTIALS DON'T MATCH"}}, 'Message'),
                            gen_tcp:send(Sock, ReplyBin),
                            authenticate(Sock)
                    end
            end
    end.