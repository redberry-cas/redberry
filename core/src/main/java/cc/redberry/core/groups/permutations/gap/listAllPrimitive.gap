filename:= "GENERATORS";
PrintTo(filename, "");
deg:=1;
while(deg < 2500) do
    count:= NrPrimitiveGroups(deg);
    counter:= 1;
    while(counter <= count) do
        g:= PrimitiveGroup(deg, counter);
        generators:= GeneratorsOfGroup(g);
        index:= 1;
        while(index <= Length(generators)) do 
            AppendTo(filename, ListPerm(generators[index], deg));
            if index < Length(generators) then
                AppendTo(filename, ", ");
            fi;
            index:= index + 1;
        od;
        AppendTo(filename, "\n");
        counter:= counter + 1;
    od;
    deg:= deg + 1;
od;