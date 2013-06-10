function Z = symbol2element(sym)
  for Z=1:1000
      if(strcmp(element2symbol(Z),sym))
          break;
      end
  end
end
