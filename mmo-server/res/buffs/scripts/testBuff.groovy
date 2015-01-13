import core.board.interfaces.Board
import core.board.interfaces.Buff

def onInit(Buff buff, Board board){
	System.out.println('init');
}

def onTick(Buff buff, Board board){
	System.out.println('tick');
}

def onEnd(Buff buff, Board board){
	System.out.println('end');
}