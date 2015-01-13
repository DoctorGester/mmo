import core.board.Board
import core.board.Buff

def onInit(Buff buff, Board board){
	System.out.println('init');
}

def onTick(Buff buff, Board board){
	out.println('tick');
}

def onEnd(Buff buff, Board board){
	out.println('end');
}