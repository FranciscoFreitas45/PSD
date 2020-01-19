-module(userpainel).
-export([start/2]).


start(Sock,Name) ->
    receive
        {tcp, Sock, Bin} ->
            Msg = messages:decode_msg(Bin,'Message'),
            case maps:get(type,Msg) of
                "OFFER" ->
                    Offer = maps:get(importerOffer,Msg),
                    OfferName = maps:put(importer,Name,Offer),
                    Msgn = maps:put(importerOffer,OfferName,Msg), % meter o nome do importador na mensagem
                    IdOrder = maps:get(idorder,Offer),
                    PidWorkerFE = taskManager:lookup(IdOrder),
                    PidWorkerFE ! {offer,Msgn,self()},
                    start(Sock,Name);
                "ORDER" ->
                    taskManager:sendOrder(Msg),
                    start(Sock,Name)
            end;
        {tcp_closed, _} ->
			loginManager:logOut(Name);
        {corder,Reply,_} ->
            Status = maps:get(res,Reply),
            IdOrder = maps:get(id,Reply),
            Prod = maps:get(product,Reply),
            case Status of
                "CANCELED" ->
                    Str = "CANCELED ORDER:" ++ integer_to_list(IdOrder) ++ " PROD: " ++ Prod,
                    sendReply(Str,Sock,"Manifactor"),
                    start(Sock,Name);
                "FINISHED" ->
                    Profit = maps:get(profit,Reply),
                    Str = "FINISHED ORDER:" ++ integer_to_list(IdOrder) ++ " PROD: " ++ Prod ++ " PROFIT:" ++ integer_to_list(Profit),
                    sendReply(Str,Sock,"Manifactor"),
                    start(Sock,Name)
            end;
        {reply,Status,H,_} ->
            IdOrder = maps:get(idorder,H),
            Prod = maps:get(product,H),
            case Status of
                "CANCELED" ->
                    Str = "CANCELED ORDER:" ++ integer_to_list(IdOrder) ++ " PROD: " ++ Prod,
                    sendReply(Str,Sock,"Importer");
                "FINISHED" ->
                    Str = "FINISHED ORDER:" ++ integer_to_list(IdOrder) ++ " PROD: " ++ Prod,
                    sendReply(Str,Sock,"Importer")
            end
    end.


sendReply(Msg,Sock,Dest) ->
    ReplyBin = messages:encode_msg(#{type => "RESPONSE", response => #{status => 1, response => Msg}}, 'Message'),
    gen_tcp:send(Sock, ReplyBin),
    io:format("Reply Send to ~s ~n",[Dest]).